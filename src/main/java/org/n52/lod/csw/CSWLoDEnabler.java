/**
 * ﻿Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.lod.csw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.opengis.cat.csw.x202.AbstractRecordType;
import net.opengis.cat.csw.x202.BriefRecordType;
import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;
import net.opengis.cat.csw.x202.GetRecordsResponseDocument;
import net.opengis.cat.csw.x202.SearchResultsType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.lod.Configuration;
import org.n52.lod.ProgressListener;
import org.n52.lod.Report;
import org.n52.lod.csw.mapping.GluesMapper;
import org.n52.lod.csw.mapping.WKTLiteralType;
import org.n52.lod.csw.mapping.XmlToRdfMapper;
import org.n52.lod.triplestore.FileTripleSink;
import org.n52.lod.triplestore.TripleSink;
import org.n52.lod.triplestore.VirtuosoServer;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.util.web.HttpClientException;
import org.purl.dc.elements.x11.SimpleLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hp.hpl.jena.datatypes.TypeMapper;

/**
 * This is the main class. It allows to start the application and to execute the
 * process of feeding all records of a CSW into an LoD triple store.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class CSWLoDEnabler {

    private static final Logger log = LoggerFactory.getLogger(CSWLoDEnabler.class);

    private static final int NUMBER_OF_RECORDS_PER_ITERATION = 25;

    private static final long FALLBACK_RECORDS_TOTAL = 10000;

    static XmlOptions xmlOptions;

    static {
        xmlOptions = new XmlOptions();
        xmlOptions.setCharacterEncoding("UTF-8");
    }

    protected boolean addToServer = false;

    protected boolean saveToFile = false;

    Configuration config = null;

    private CatalogInteractor csw;
    
    private ProgressListener progressListener;

    protected Report report = new Report();

    protected final Map<String, GetRecordByIdResponseDocument> POISON_PILL = Maps.newHashMap();

    public CSWLoDEnabler(Configuration config) {
        
        WKTLiteralType type = WKTLiteralType.theWKTLiteralType;
        
        this.progressListener = config.getProgressListener();
        
        TypeMapper.getInstance().registerDatatype(type);
        
        POISON_PILL.put("poison", null);

        addToServer = config.isAddToServer();
        saveToFile = config.isSaveToFile();

        this.config = config;
        this.csw = new CatalogInteractor(config);
        log.info("NEW {}", this);
    }

    public static void main(String[] args) {
        try {
            CSWLoDEnabler enabler = new CSWLoDEnabler(new Configuration(Configuration.DEFAULT_CONFIG_FILE));
            enabler.runOverAll();
        } catch (RuntimeException | IOException e) {
            log.error("Error running CSW to LOD", e);
        }
    }

    public void runOverAll() throws IOException {
        runStartingFrom(1);
    }

    /**
     * executes the program: 1.) retrieves the record descriptions from the CSW
     * 2.) transforms the descriptions to RDF 3.) inserts the produced RDF into
     * the triplestore
     * 
     * @param startPos
     * 
     * @throws IOException
     */
    public void runStartingFrom(int startPos) throws IOException {
        log.info("STARTING CSW to LOD..");

        if (!(addToServer || saveToFile)) {
            log.warn("Neither triple store nor file output are activated.");
            return;
        }

        final Stopwatch overallTimer = new Stopwatch();
        overallTimer.start();

        final Stopwatch retrievingTimer = new Stopwatch();
        final Stopwatch mappingTimer = new Stopwatch();
        final Stopwatch otherTimer = new Stopwatch();

        otherTimer.start();
        XmlToRdfMapper mapper = new GluesMapper(config);

        TripleSink serverSink = null;
        if (addToServer) {
            try {
                serverSink = new VirtuosoServer(config, mapper);
            } catch (RuntimeException e) {
                log.error("Could not connect to graph", e);
            }
        }

        TripleSink fileSink = null;
        if (saveToFile) {
            fileSink = new FileTripleSink(mapper);
        }

        long recordsInTotal = FALLBACK_RECORDS_TOTAL;
        try {
            recordsInTotal = csw.getNumberOfRecords();
            log.debug("Retrieved number of records from server: {}", recordsInTotal);
        } catch (IllegalStateException | HttpClientException | XmlException e) {
            log.error("Could not retrieve number of records from catalog {}, falling back to {}", csw, FALLBACK_RECORDS_TOTAL, e);
        }
        report.startIndex = startPos;
        report.recordNumber = recordsInTotal;
        otherTimer.stop();

        // main loop
        while (startPos < recordsInTotal) {
            retrievingTimer.start();
            Map<String, GetRecordByIdResponseDocument> records = retrieveRecords(startPos, NUMBER_OF_RECORDS_PER_ITERATION, recordsInTotal);
            retrievingTimer.stop();

            mappingTimer.start();
            if (addToServer && serverSink != null)
                serverSink.addRecords(records, report);
            if (saveToFile && fileSink != null)
                fileSink.addRecords(records, report);
            mappingTimer.stop();

            startPos = startPos + NUMBER_OF_RECORDS_PER_ITERATION;

            log.debug("Finished intermediate run at {}", overallTimer.toString());
        } // end of main loop

        otherTimer.start();
        if (fileSink != null)
            try {
                fileSink.close();
            } catch (Exception e) {
                log.error("Could not close file sink {}", fileSink, e);
            }

        if (serverSink != null)
            try {
                serverSink.close();
            } catch (Exception e) {
                log.error("Could not close server sink {}", serverSink, e);
            }

        if (!report.issues.isEmpty())
            log.error(report.extendedToString());

        overallTimer.stop();
        otherTimer.stop();

        log.info("DONE with CSW to LOD.. duration = {} (retrieving: {}, mapping = {}, other = {})", overallTimer, retrievingTimer, mappingTimer, otherTimer);
        log.info("Results: {}", report);
        log.info("Sinks: server = {}, file = {}", addToServer, saveToFile);
        log.info("Server: {} | File: {}", serverSink, fileSink);
    }

    public void asyncRunStartingFrom(final int startPos) throws IOException {
        log.info("STARTING CSW to LOD..");

        if (!(addToServer || saveToFile)) {
            log.warn("Neither triple store nor file output are activated.");
            return;
        }

        final Stopwatch overallTimer = new Stopwatch();
        overallTimer.start();

        final Stopwatch retrievingTimer = new Stopwatch();
        final Stopwatch mappingTimer = new Stopwatch();
        final Stopwatch otherTimer = new Stopwatch();

        otherTimer.start();
        XmlToRdfMapper mapper = new GluesMapper(config);

        TripleSink serverSink = null;
        if (addToServer) {
            try {
                serverSink = new VirtuosoServer(config, mapper);
            } catch (RuntimeException e) {
                log.error("Could not connect to graph", e);
            }
        }

        TripleSink fileSink = null;
        if (saveToFile) {
            fileSink = new FileTripleSink(mapper);
        }

        long recordsInTotal;
        try {
            recordsInTotal = csw.getNumberOfRecords();
            log.debug("Retrieved number of records from server: {}", recordsInTotal);
        } catch (IllegalStateException | HttpClientException | XmlException e) {
            log.error("Could not retrieve number of records from catalog {}, falling back to {}", csw, FALLBACK_RECORDS_TOTAL, e);
            recordsInTotal = FALLBACK_RECORDS_TOTAL;
        }
        report.startIndex = startPos;
        report.recordNumber = recordsInTotal;
        otherTimer.stop();

        async(startPos, recordsInTotal, overallTimer, retrievingTimer, mappingTimer, serverSink, fileSink);

        otherTimer.start();
        if (fileSink != null)
            try {
                fileSink.close();
            } catch (Exception e) {
                log.error("Could not close file sink {}", fileSink, e);
            }

        if (serverSink != null)
            try {
                serverSink.close();
            } catch (Exception e) {
                log.error("Could not close server sink {}", serverSink, e);
            }

        if (!report.issues.isEmpty())
            log.error(report.extendedToString());

        overallTimer.stop();
        otherTimer.stop();
        
        log.info("DONE with CSW to LOD.. duration = {} (retrieving: {}, mapping = {}, other = {})", overallTimer, retrievingTimer, mappingTimer, otherTimer);
        
        log.info("Results: {}", report);
        log.info("Sinks: server = {}, file = {}", addToServer, saveToFile);
        log.info("Server: {} | File: {}", serverSink, fileSink);
    }

    private void async(final int startPos,
            final long recordCount,
            final Stopwatch overallTimer,
            final Stopwatch retrievingTimer,
            final Stopwatch mappingTimer,
            final TripleSink serverSink,
            final TripleSink fileSink) {
        // processing queue
        final ConcurrentLinkedQueue<Map<String, GetRecordByIdResponseDocument>> queue = Queues.newConcurrentLinkedQueue();

        // main loop download - producer
        ExecutorService downloadExecutor = Executors.newSingleThreadExecutor();
        downloadExecutor.submit(new Runnable() {

            private final Logger logger = LoggerFactory.getLogger("Download Runnable");

            @Override
            public void run() {
                int i = startPos;
                while (i < recordCount) {
                    retrievingTimer.start();
                    // Map<String, GetRecordByIdResponseDocument> records =
                    // retrieveRecords(i, NUMBER_OF_RECORDS_PER_ITERATION,
                    // recordCount);
                    Map<String, GetRecordByIdResponseDocument> records = retrieveRecordsThreaded(i, NUMBER_OF_RECORDS_PER_ITERATION, recordCount);
                    queue.add(records);
                    retrievingTimer.stop();

                    i = i + NUMBER_OF_RECORDS_PER_ITERATION;
                    logger.debug("Finished intermediate download run at {}", overallTimer.toString());
                    logger.info("Retrieved {} records, queue size is now {}", records.size(), queue.size());
                } // end of main loop

                logger.trace("Done - adding the poison pill!");
                queue.add(POISON_PILL);
            }
        });

        // consumer
        ExecutorService mapExecutor = Executors.newSingleThreadExecutor();
        mapExecutor.submit(new Runnable() {

            private final Logger logger = LoggerFactory.getLogger("Map Runnable");

            private boolean isRunning = true;

            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.error("Error sleeping in mapping runnable", e);
                    }

                    try {
                        Map<String, GetRecordByIdResponseDocument> records = queue.poll();

                        if (records == null)
                            continue;

                        if (records == POISON_PILL) {
                            queue.add(POISON_PILL); // notify other threads to
                                                    // stop
                            isRunning = false;
                            logger.trace("Got the poison pill!");
                            return;
                        }

                        // process queueElement
                        mappingTimer.start();
                        if (addToServer && serverSink != null)
                            serverSink.addRecords(records, report);
                        if (saveToFile && fileSink != null)
                            fileSink.addRecords(records, report);
                        mappingTimer.stop();

                        logger.debug("Finished intermediate run at {}", overallTimer.toString());

                    } catch (RuntimeException e) {
                        logger.error("Error in mapping runnable", e);
                    }
                } // end of main loop
            }
        });

        downloadExecutor.shutdown();
        try {
            downloadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("during shut down of download executor", e);
        }
        mapExecutor.shutdown();
        try {
            mapExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("during shut down of map executor", e);
        }
    }

    protected Map<String, GetRecordByIdResponseDocument> retrieveRecords(int startPos,
            int maxRecords,
            long recordsInTotal) {
    	
    	String progressInfo = String.format("Retrieve %d records, starting from %d of %d", maxRecords, startPos, recordsInTotal);
    	
    	if(this.progressListener != null){
    		progressListener.updateProgress((int) (((double)startPos / (double)recordsInTotal) * 100));
    	}
    	
        log.info(progressInfo);

        List<String> recordIdList = getRecordIds(startPos, maxRecords);

        Map<String, GetRecordByIdResponseDocument> recordDescriptions = getRecordDescriptions(startPos, maxRecords, recordsInTotal, recordIdList);

        log.info("Done with requests and parsing, have {} GetRecordById documents.", recordDescriptions.size());
        return recordDescriptions;
    }

    private Map<String, GetRecordByIdResponseDocument> getRecordDescriptions(int startPos,
            int maxRecords,
            long recordsInTotal,
            List<String> recordIdList) {
        Map<String, GetRecordByIdResponseDocument> recordDescriptions = Maps.newHashMap();

        int i = 0;
        for (String id : recordIdList) {
            log.debug("Retrieving details of record {}/{}  (over all {}/{}): {}", i, maxRecords, startPos + i, recordsInTotal, id);

            try {
                String recordDescription = csw.executeGetRecordsById(id);
                GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(recordDescription, xmlOptions);

                recordDescriptions.put(id, xb_getRecordByIdResponse);
            } catch (OXFException | RuntimeException | ExceptionReport | XmlException e) {
                log.error("Error retrieving and parsing record {} with id {}", i, id, e);
                report.retrievalIssues.put(id, e);
            }

            i++;
        }
        return recordDescriptions;
    }

    private List<String> getRecordIds(int startPos,
            int maxRecords) {
        ArrayList<String> recordIdList = Lists.newArrayList();

        SearchResultsType searchResults = null;
        try {
            String result = csw.executeGetRecords(maxRecords, startPos);
            GetRecordsResponseDocument responseDoc = GetRecordsResponseDocument.Factory.parse(result);
            searchResults = responseDoc.getGetRecordsResponse().getSearchResults();

        } catch (OXFException | ExceptionReport | XmlException e) {
            log.error("Could not retrieving and parsing records {} to {}", startPos, startPos + maxRecords, e);
            report.retrievalIssues.put("Request for " + startPos + " - " + startPos + maxRecords + " failed.", e);
            return Lists.newArrayList();
        }

        AbstractRecordType[] abstractRecordArray = searchResults.getAbstractRecordArray();
        for (AbstractRecordType abstractRecordType : abstractRecordArray) {
            try {
                BriefRecordType abstractRecord = BriefRecordType.Factory.parse(abstractRecordType.xmlText());
                if (abstractRecord.getIdentifierArray() != null && abstractRecord.getIdentifierArray().length >= 1) {
                    SimpleLiteral identifierLiteral = SimpleLiteral.Factory.parse(abstractRecord.getIdentifierArray(0).getDomNode());
                    String recordId = identifierLiteral.getDomNode().getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                    recordIdList.add(recordId);
                }
            } catch (XmlException e) {
                log.error("Could not parse record {}", abstractRecordType.xmlText(), e);
                report.retrievalIssues.put("Parsing records response", e);
                return Lists.newArrayList();
            }
        }
        log.debug("Found {} record ids based on catalog response with {} matched and {} returned", recordIdList.size(), searchResults.getNumberOfRecordsMatched(),
                searchResults.getNumberOfRecordsReturned());
        return recordIdList;
    }

    private class CallableRecordDescription implements Callable<GetRecordByIdResponseDocument> {

        private final Logger logger = LoggerFactory.getLogger(CallableRecordDescription.class);

        private String id;

        private CatalogInteractor csw;

        public CallableRecordDescription(String id, CatalogInteractor csw) {
            this.id = id;
            this.csw = csw;
        }

        @Override
        public GetRecordByIdResponseDocument call() throws Exception {
            logger.debug("Retrieving {} using {}", this.id, this.csw);
            String recordDescription = this.csw.executeGetRecordsById(this.id);
            GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(recordDescription, xmlOptions);
            return xb_getRecordByIdResponse;
        }

    }

    protected Map<String, GetRecordByIdResponseDocument> retrieveRecordsThreaded(int startPos,
            int maxRecords,
            long recordsInTotal) {
        log.info("Retrieve {} records, starting from {} of {}", maxRecords, startPos, recordsInTotal);

        // one thread for getting ids
        List<String> recordIdList = getRecordIds(startPos, maxRecords);

        // many threads getting records descriptions
        final Map<String, GetRecordByIdResponseDocument> recordDescriptions = Maps.newConcurrentMap();

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(maxRecords));

        for (String id : recordIdList) {
            final String recordId = id;
            log.debug("Adding {} to the model", recordId);

            CallableRecordDescription c = new CallableRecordDescription(id, csw);
            ListenableFuture<GetRecordByIdResponseDocument> responseFuture = executorService.submit(c);

            Futures.addCallback(responseFuture, new FutureCallback<GetRecordByIdResponseDocument>() {

                private final Logger logger = LoggerFactory.getLogger("Record Downloader");

                @Override
                public void onFailure(Throwable t) {
                    logger.error("Error retrieving and parsing record {}", t);
                    report.retrievalIssues.put(recordId, t);
                }

                @Override
                public void onSuccess(GetRecordByIdResponseDocument result) {
                    logger.trace("SUCCESS with {}", result);
                    recordDescriptions.put(recordId, result);

                    report.added++;
                    report.addedIds.add(recordId);
                }

            });
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                log.error("Could not await termination", e);
            }
        }

        log.info("Done with requests and parsing, have {} GetRecordById documents.", recordDescriptions.size());
        return recordDescriptions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSWLoDEnabler [addToTripleStore=").append(addToServer).append(", saveToFile=").append(saveToFile).append("]");
        return builder.toString();
    }

    public Report getReport() {
        return report;
    }

}
