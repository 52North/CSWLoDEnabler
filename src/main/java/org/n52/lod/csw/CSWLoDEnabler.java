package org.n52.lod.csw;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
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

    private boolean addToTripleStore = false;

    private boolean saveToFile = false;

    Constants constants = null;

    private CatalogInteractor csw;

    private Report report;

    private static class Report {

        public int added = 0;

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
            builder.append(", ");
            if (addedIds != null) {
                builder.append("\n\t\taddedIds=");
                builder.append(Arrays.toString(addedIds.toArray()));
                builder.append(", ");
            }
            if (issues != null) {
                builder.append("\n\t\tissue ids=");
                builder.append(Arrays.toString(issues.keySet().toArray()));
            }
            if (issues != null) {
                builder.append("\n\t\tretrievalIssueIds=");
                builder.append(Arrays.toString(retrievalIssues.keySet().toArray()));
            }
            builder.append("]");
            return builder.toString();
        }

        public String extendedToString() {
            StringBuilder sb = new StringBuilder();
            sb.append(toString());
            sb.append("\n\n\n********************** Issues **********************\n");
            sb.append(Joiner.on(" ").withKeyValueSeparator(":").join(issues));
            sb.append("\n\n\n***************** Retrieval Issues *****************\n");
            sb.append(Joiner.on("\n").withKeyValueSeparator(":").join(retrievalIssues));
            return sb.toString();
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

    private void runOverAll() throws IOException {
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

        GraphBase graph = null;
        if (addToTripleStore) {
            try {
                graph = new VirtGraph(constants.getUriGraph(), constants.getUrlVirtuosoJdbc(), constants.getVirtuosoUser(), constants.getVirtuosoPass());
            } catch (RuntimeException e) {
                log.error("Could not connect to graph", e);
            }
        }

        Model fileModel = null;
        File tempDir = null;
        if (saveToFile) {
            tempDir = Files.createTempDir();
            ModelMaker fileModelMaker = ModelFactory.createFileModelMaker(tempDir.getAbsolutePath());
            fileModel = configureModel(fileModelMaker.createDefaultModel());
            log.info("Saving triples to files in {}", tempDir);
        }

        long recordsInTotal = FALLBACK_RECORDS_TOTAL;
        try {
            recordsInTotal = csw.getNumberOfRecords();
            log.debug("Retrieved number of records from server: {}", recordsInTotal);
        } catch (IllegalStateException | HttpClientException | XmlException e) {
            log.error("Could not retrieve number of records from catalog {}, falling back to {}", csw, FALLBACK_RECORDS_TOTAL, e);
        }
        report.recordNumber = recordsInTotal;

        while (startPos < recordsInTotal) {
            Map<String, GetRecordByIdResponseDocument> records = null;
            try {
                records = retrieveRecords(startPos, NUMBER_OF_RECORDS_PER_ITERATION, recordsInTotal);
            } catch (OXFException | ExceptionReport | XmlException | RuntimeException e) {
                int endPos = startPos + NUMBER_OF_RECORDS_PER_ITERATION;
                log.error("Error while adding {} to {} to triple store, skipping one, trying again...", startPos, endPos, e);
                // not a nice hack, skipping one entry
                startPos++;
                report.retrievalIssues.put("Request for " + startPos + " - " + endPos + " failed.", e);
                continue;
            }

            if (addToTripleStore)
                addRecordsToTripleStore(records, graph);
            if (saveToFile)
                addRecordsToModel(records, fileModel);

            startPos = startPos + NUMBER_OF_RECORDS_PER_ITERATION;
        }

        if (saveToFile && fileModel != null && tempDir != null) {
            log.debug("Saved files (in {}) for model of size {}", tempDir, fileModel.size());
            fileModel.close();
        }

        long timeDuration = System.currentTimeMillis() - timeStart;
        log.info("DONE with CSW to LOD.. duration = {} | {}", timeDuration, new Date(timeDuration));
        log.info("Results: {}", report);
        if (!report.issues.isEmpty())
            log.error(report.extendedToString());
    }

    private void addRecordsToTripleStore(Map<String, GetRecordByIdResponseDocument> records,
            GraphBase graph) {
        Model tripleStoreModel = configureModel(ModelFactory.createModelForGraph(graph));
        log.info("Processing {} record descriptions into model {}", records.size(), tripleStoreModel);

        addRecordsToModel(records, tripleStoreModel);
        log.debug("DONE - model: {} | graph: {}", tripleStoreModel, graph);

        tripleStoreModel.close();
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

        log.info("Added {} of {} records to model, which now has size {}", addedCounter, records.size(), model.size());
    }

    private Map<String, GetRecordByIdResponseDocument> retrieveRecords(int startPos,
            int maxRecords,
            long recordsInTotal) throws OXFException, ExceptionReport, XmlException {
        log.info("Retrieve {} records, starting from {}, of {}", maxRecords, startPos, recordsInTotal);

        String result = csw.executeGetRecords(maxRecords, startPos);

        GetRecordsResponseDocument responseDoc = GetRecordsResponseDocument.Factory.parse(result);
        SearchResultsType searchResults = responseDoc.getGetRecordsResponse().getSearchResults();

        // collect all record IDs:
        List<String> recordIdList = Lists.newArrayList();
        AbstractRecordType[] abstractRecordArray = searchResults.getAbstractRecordArray();
        for (AbstractRecordType abstractRecordType : abstractRecordArray) {
            BriefRecordType abstractRecord = BriefRecordType.Factory.parse(abstractRecordType.xmlText());

            if (abstractRecord.getIdentifierArray() != null && abstractRecord.getIdentifierArray().length >= 1) {

                SimpleLiteral identifierLiteral = SimpleLiteral.Factory.parse(abstractRecord.getIdentifierArray(0).getDomNode());
                String recordId = identifierLiteral.getDomNode().getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                recordIdList.add(recordId);
            }
        }

        log.debug("Found {} record ids based on catalog response with {} matched and {} returned", recordIdList.size(), searchResults.getNumberOfRecordsMatched(),
                searchResults.getNumberOfRecordsReturned());

        Map<String, GetRecordByIdResponseDocument> recordDescriptions = Maps.newHashMap();
        int i = 0;
        for (String id : recordIdList) {
            log.debug("Retrieving details of record {}/{}  (over all {}/{}): {}", i, maxRecords, startPos + i, recordsInTotal, id);

            try {
                String recordDescription = csw.executeGetRecordsById(id);
                GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(recordDescription, xmlOptions);

                recordDescriptions.put(id, xb_getRecordByIdResponse);
            } catch (OXFException e) {
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
