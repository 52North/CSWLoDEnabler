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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opengis.cat.csw.x202.AbstractRecordType;
import net.opengis.cat.csw.x202.BriefRecordType;
import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;
import net.opengis.cat.csw.x202.GetRecordsResponseDocument;
import net.opengis.cat.csw.x202.SearchResultsType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.lod.csw.mapping.IsoToRdfMapper;
import org.n52.lod.vocab.PROV;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.util.web.HttpClientException;
import org.purl.dc.elements.x11.SimpleLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtGraph;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

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

    protected boolean addToTripleStore = false;

    protected boolean saveToFile = false;

    Constants constants = null;

    private CatalogInteractor csw;

    protected Report report;

    protected static class Report {

        public int added = 0;

        public long startIndex = 0;

        public long recordNumber = 0;

        public List<String> addedIds = Lists.newArrayList();

        public Map<String, Object> retrievalIssues = Maps.newHashMap();

        public Map<String, Object> issues = Maps.newHashMap();

        public Report() {
            //
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Report [added=");
            builder.append(added);
            builder.append(", recordNumber=");
            builder.append(recordNumber);
            builder.append(", startIndex=");
            builder.append(startIndex);
            if (addedIds != null) {
                builder.append(", # added=");
                builder.append(addedIds.size());
            }
            if (issues != null) {
                builder.append(", # issues=");
                builder.append(issues.size());
            }
            if (issues != null) {
                builder.append(", # retrieval issue=");
                builder.append(retrievalIssues.size());
            }
            builder.append("]");
            return builder.toString();
        }

        public String extendedToString() {
            StringBuilder builder = new StringBuilder();
            builder.append(toString());
            if (addedIds != null) {
                builder.append("\n\n\n\n\n********************** Added **********************\n");
                builder.append(Arrays.toString(addedIds.toArray()));
                builder.append(", ");
            }
            builder.append("\n\n\n\n\n********************** Issues **********************\n");
            builder.append(Joiner.on("\n\n").withKeyValueSeparator(" : ").join(issues));
            builder.append("\n\n\n\n\n***************** Retrieval Issues *****************\n");
            builder.append(Joiner.on("\n\n").withKeyValueSeparator(" : ").join(retrievalIssues));
            builder.append("\n\n");
            builder.append(toString());
            return builder.toString();
        }
    }

    public CSWLoDEnabler(boolean addToTripleStore, boolean saveToFile) throws IOException {
        this.addToTripleStore = addToTripleStore;
        this.saveToFile = saveToFile;
        this.report = new Report();

        constants = Constants.getInstance();
        csw = new CatalogInteractor();

        log.info("NEW {}", this);
    }

    public static void main(String[] args) {
        try {
            CSWLoDEnabler enabler = new CSWLoDEnabler(true, true);
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

        if (!(addToTripleStore || saveToFile)) {
            log.warn("Neither triple store nor file output are activated.");
            return;
        }

        long timeStart = System.currentTimeMillis();

        Model tripleStoreModel = null;
        if (addToTripleStore) {
            try {
                GraphBase graph = new VirtGraph(constants.getUriGraph(), constants.getUrlVirtuosoJdbc(), constants.getVirtuosoUser(), constants.getVirtuosoPass());
                tripleStoreModel = configureModel(ModelFactory.createModelForGraph(graph));
            } catch (RuntimeException e) {
                log.error("Could not connect to graph", e);
            }
        }

        Model fileModel = null;
        if (saveToFile) {
            ModelMaker fileModelMaker = ModelFactory.createMemModelMaker(); // createFileModelMaker(tempDir.getAbsolutePath());
            fileModel = configureModel(fileModelMaker.createDefaultModel());
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

        while (startPos < recordsInTotal) {
            Map<String, GetRecordByIdResponseDocument> records = null;
            records = retrieveRecords(startPos, NUMBER_OF_RECORDS_PER_ITERATION, recordsInTotal);

            if (addToTripleStore && tripleStoreModel != null)
                addRecordsToModel(records, tripleStoreModel);
            if (saveToFile && fileModel != null)
                addRecordsToModel(records, fileModel);

            startPos = startPos + NUMBER_OF_RECORDS_PER_ITERATION;
        }

        if (saveToFile && fileModel != null) {
            File rdf = File.createTempFile("csw2lod_model_", ".xml");
            long size = fileModel.size();
            fileModel.write(Files.newOutputStreamSupplier(rdf).getOutput(), "RDF/XML");
            Path turtle = rdf.toPath().resolveSibling(rdf.toPath().getFileName().toString().replace("xml", "ttl"));
            fileModel.write(Files.newOutputStreamSupplier(turtle.toFile()).getOutput(), "TURTLE");
            fileModel.close();

            log.debug("Saved model in files {} and {}, model size {}", rdf, turtle, size);
        }

        if (tripleStoreModel != null)
            tripleStoreModel.close();

        long timeDuration = System.currentTimeMillis() - timeStart;
        log.info("DONE with CSW to LOD.. duration = {} | {} minutes ", timeDuration, timeDuration / 1000 / 60);
        log.info("Results: {}", report);
        if (!report.issues.isEmpty())
            log.error(report.extendedToString());
    }

    private Model configureModel(Model model) {
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("dc", DC_11.getURI());
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("vcard", VCARD.getURI());
        model.setNsPrefix("prov", PROV.getURI());
        return model;
    }

    private void addRecordsToModel(Map<String, GetRecordByIdResponseDocument> records,
            Model model) {
        IsoToRdfMapper mapper = new IsoToRdfMapper();
        int addedCounter = 0;

        Model result = model;
        for (Entry<String, GetRecordByIdResponseDocument> entry : records.entrySet()) {
            log.debug("Adding {} to the model", entry.getKey());

            try {
                result = mapper.addGetRecordByIdResponseToModel(model, entry.getValue());
                if (result != null) {
                    addedCounter++;
                    this.report.added++;
                    this.report.addedIds.add(entry.getKey());
                } else {
                    this.report.issues.put(entry.getKey(), "Error while adding to model: " + entry.getValue().xmlText());
                    result = model;
                }
            } catch (OXFException | XmlException | IOException e) {
                log.error("Error processing record {}", entry.getKey(), e);
                this.report.issues.put(entry.getKey(), e);
            }
        }

        log.info("Added {} of {} records to model {}, which now has size {}", addedCounter, records.size(), model.getClass(), model.size());
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
        log.debug("Done with requests and parsing, have {} GetRecordById documents.", recordDescriptions.size());
        return recordDescriptions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSWLoDEnabler [addToTripleStore=").append(addToTripleStore).append(", saveToFile=").append(saveToFile).append("]");
        return builder.toString();
    }

}
