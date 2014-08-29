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
import java.util.List;
import java.util.Map;

import net.opengis.cat.csw.x202.AbstractRecordType;
import net.opengis.cat.csw.x202.BriefRecordType;
import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;
import net.opengis.cat.csw.x202.GetRecordsResponseDocument;
import net.opengis.cat.csw.x202.SearchResultsType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.lod.Configuration;
import org.n52.lod.Report;
import org.n52.lod.csw.mapping.GluesMapper;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This is the main class. It allows to start the application and to execute the
 * process of feeding all records of a CSW into an LoD triple store.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class CSWLoDEnabler {

    private static final Logger log = LoggerFactory.getLogger(CSWLoDEnabler.class);

    private static final int NUMBER_OF_RECORDS_PER_ITERATION = 50;

    private static final long FALLBACK_RECORDS_TOTAL = 10000;

    private static XmlOptions xmlOptions;

    static {
        xmlOptions = new XmlOptions();
        xmlOptions.setCharacterEncoding("UTF-8");
    }

    protected boolean addToServer = false;

    protected boolean saveToFile = false;

    Configuration config = null;

    private CatalogInteractor csw;

    protected Report report = new Report();

    public CSWLoDEnabler(Configuration config) {
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

        long timeStart = System.currentTimeMillis();

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

        // main loop
        while (startPos < recordsInTotal) {
            Map<String, GetRecordByIdResponseDocument> records = null;
            records = retrieveRecords(startPos, NUMBER_OF_RECORDS_PER_ITERATION, recordsInTotal);

            if (addToServer && serverSink != null)
                serverSink.addRecords(records, report);
            if (saveToFile && fileSink != null)
                fileSink.addRecords(records, report);

            startPos = startPos + NUMBER_OF_RECORDS_PER_ITERATION;
        } // end of main loop

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

        long timeDuration = System.currentTimeMillis() - timeStart;
        if (!report.issues.isEmpty())
            log.error(report.extendedToString());

        log.info("DONE with CSW to LOD.. duration = {} | {} minutes ", timeDuration, timeDuration / 1000 / 60);
        log.info("Results: {}", report);
        log.info("Sinks: server = {}, file = {}", addToServer, saveToFile);
        log.info("Server: {} | File: {}", serverSink, fileSink);
    }

    private Map<String, GetRecordByIdResponseDocument> retrieveRecords(int startPos,
            int maxRecords,
            long recordsInTotal) {
        log.info("Retrieve {} records, starting from {} of {}", maxRecords, startPos, recordsInTotal);
        Map<String, GetRecordByIdResponseDocument> recordDescriptions = Maps.newHashMap();

        SearchResultsType searchResults = null;
        try {
            String result = csw.executeGetRecords(maxRecords, startPos);
            GetRecordsResponseDocument responseDoc = GetRecordsResponseDocument.Factory.parse(result);
            searchResults = responseDoc.getGetRecordsResponse().getSearchResults();

        } catch (OXFException | ExceptionReport | XmlException e) {
            log.error("Could not retrieving and parsing records {} to {}", startPos, startPos + maxRecords, e);
            report.retrievalIssues.put("Request for " + startPos + " - " + startPos + maxRecords + " failed.", e);
            return recordDescriptions;
        }

        // collect all record IDs:
        List<String> recordIdList = Lists.newArrayList();
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
                return recordDescriptions;
            }
        }

        log.debug("Found {} record ids based on catalog response with {} matched and {} returned", recordIdList.size(), searchResults.getNumberOfRecordsMatched(),
                searchResults.getNumberOfRecordsReturned());

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
        
        log.info("Done with requests and parsing, have {} GetRecordById documents.", recordDescriptions.size());
        return recordDescriptions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSWLoDEnabler [addToTripleStore=").append(addToServer).append(", saveToFile=").append(saveToFile).append("]");
        return builder.toString();
    }

}
