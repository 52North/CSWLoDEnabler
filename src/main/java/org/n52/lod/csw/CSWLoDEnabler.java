package org.n52.lod.csw;

import java.io.File;
import java.io.IOException;
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
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.util.web.HttpClientException;
import org.purl.dc.elements.x11.SimpleLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

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

    public CSWLoDEnabler(boolean addToTripleStore, boolean saveToFile) throws IOException {
        this.addToTripleStore = addToTripleStore;
        this.saveToFile = saveToFile;
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

    /**
     * executes the program: 1.) retrieves the record descriptions from the CSW
     * 2.) transforms the descriptions to RDF 3.) inserts the produced RDF into
     * the triplestore
     * 
     * @throws IOException
     */
    public void runOverAll() throws IOException {
        log.info("STARTING CSW to LOD..");

        if (!(addToTripleStore || saveToFile)) {
            log.warn("Neither triple store nor file output are activated.");
            return;
        }

        long timeStart = System.currentTimeMillis();

        long recordsInTotal;
        try {
            recordsInTotal = csw.getNumberOfRecords();
            log.debug("Retrieved number of records from server: {}", recordsInTotal);
        } catch (IllegalStateException | HttpClientException | XmlException e) {
            log.error("Could not retrieve number of records from catalog {}, falling back to {}", csw, FALLBACK_RECORDS_TOTAL, e);
            recordsInTotal = FALLBACK_RECORDS_TOTAL;
        }
        int startPos = 1;

        GraphBase graph = null;
        if (addToTripleStore) {
            try {
                graph = new VirtGraph(constants.getUriGraph(), constants.getUrlVirtuosoJdbc(), constants.getVirtuosoUser(), constants.getVirtuosoPass());
            } catch (RuntimeException e) {
                log.error("Could not connect to graph", e);
            }
        }

        Model fileModel = null;
        if (saveToFile) {
            File tempDir = Files.createTempDir();
            ModelMaker fileModelMaker = ModelFactory.createFileModelMaker(tempDir.getAbsolutePath());
            fileModel = fileModelMaker.createDefaultModel();
            log.info("Saving triples to files in {}", tempDir);
        }

        while (startPos < recordsInTotal) {
            try {
                Map<String, GetRecordByIdResponseDocument> records = retrieveRecords(startPos, NUMBER_OF_RECORDS_PER_ITERATION);

                if (addToTripleStore)
                    addRecordsToTripleStore(records, graph);
                if (saveToFile)
                    addRecordsToModel(records, fileModel);
            } catch (OXFException | ExceptionReport | XmlException e) {
                log.error("Could not add records {} to {} to triple store", startPos, startPos + NUMBER_OF_RECORDS_PER_ITERATION, e);
            }

            startPos = startPos + NUMBER_OF_RECORDS_PER_ITERATION;
        }

        long timeDuration = System.currentTimeMillis() - timeStart;
        log.info("DONE with CSW to LOD.. duration = {}", timeDuration);
    }

    private void addRecordsToTripleStore(Map<String, GetRecordByIdResponseDocument> records,
            GraphBase graph) {
        Model tripleStoreModel = ModelFactory.createModelForGraph(graph);
        log.info("Processing {} record descriptions into model {}", records.size(), tripleStoreModel);

        addRecordsToModel(records, tripleStoreModel);
        log.debug("DONE - model: {} | graph: {}", tripleStoreModel, graph);

        tripleStoreModel.close();
    }

    private void addRecordsToModel(Map<String, GetRecordByIdResponseDocument> records,
            Model model) {
        IsoToRdfMapper mapper = new IsoToRdfMapper();
        int addedCounter = 0;
        for (Entry<String, GetRecordByIdResponseDocument> entry : records.entrySet()) {
            log.debug("Addingd {} to the model", entry.getKey());

            try {
                model = mapper.addGetRecordByIdResponseToModel(model, entry.getValue());
                addedCounter++;
            } catch (OXFException | XmlException | IOException e) {
                log.error("Error processing record {}", entry.getKey(), e);
            }
        }

        log.info("ADDED {} of {} records to model: {}", addedCounter, records.size(), model);
    }

    private Map<String, GetRecordByIdResponseDocument> retrieveRecords(int startPos,
            int maxRecords) throws OXFException, ExceptionReport, XmlException {
        log.info("Retrieve {} records (max) starting from {}", maxRecords, startPos);

        
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
            log.debug("Processing record number {}/{}: {}", i, maxRecords, id);

            try {
                String recordDescription = csw.executeGetRecordsById(id);
                GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(recordDescription, xmlOptions);

                recordDescriptions.put(id, xb_getRecordByIdResponse);
            } catch (OXFException e) {
                log.error("Error processing record {}", i, e);
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
