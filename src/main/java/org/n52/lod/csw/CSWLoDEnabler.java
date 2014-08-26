package org.n52.lod.csw;

import java.util.ArrayList;
import java.util.List;

import net.opengis.cat.csw.x202.AbstractRecordType;
import net.opengis.cat.csw.x202.BriefRecordType;
import net.opengis.cat.csw.x202.GetRecordsResponseDocument;
import net.opengis.cat.csw.x202.SearchResultsType;

import org.jfree.util.Log;
import org.n52.lod.csw.mapping.IsoToRdfMapper;
import org.n52.oxf.OXFException;
import org.purl.dc.elements.x11.SimpleLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This is the main class. It allows to start the application and to execute the
 * process of feeding all records of a CSW into an LoD triple store.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class CSWLoDEnabler {

    private static final Logger log = LoggerFactory.getLogger(CSWLoDEnabler.class);

    public static void main(String[] args) {
        try {
            runOverAll();
        } catch (Exception e) {
            log.error("Error running CSW to LOD", e);
        }
    }

    public static void runOverAll() throws Exception {
        log.info("STARTING CSW to LOD..");

        long timeStart = System.currentTimeMillis();

        int recordsInTotal = 2427;
        int startPos = 1;

        while (startPos < recordsInTotal) {
            run(startPos, 100);
            startPos = startPos + 100;
        }

        long timeDuration = System.currentTimeMillis() - timeStart;
        log.info("DONE. duration = {}", timeDuration);
    }

    /**
     * executes the program: 1.) retrieves the record descriptions from the CSW
     * 2.) transforms the descriptions to RDF 3.) inserts the produced RDF into
     * the triplestore
     * 
     * @throws Exception
     */
    private static void run(int startPos,
            int maxRecords) throws Exception {
        log.info("Transfer {} records (max) from {}", maxRecords, startPos);
        Constants cons = Constants.getInstance();

        CatalogInteractor csw = new CatalogInteractor();

        String result = csw.executeGetRecords(maxRecords, startPos);

        GetRecordsResponseDocument responseDoc = GetRecordsResponseDocument.Factory.parse(result);
        SearchResultsType searchResults = responseDoc.getGetRecordsResponse().getSearchResults();

        // collect all record IDs:
        List<String> recordIdList = new ArrayList<String>();
        AbstractRecordType[] abstractRecordArray = searchResults.getAbstractRecordArray();
        for (int i = 0; i < abstractRecordArray.length; i++) {
            BriefRecordType abstractRecord = BriefRecordType.Factory.parse(abstractRecordArray[i].xmlText());

            if (abstractRecord.getIdentifierArray() != null && abstractRecord.getIdentifierArray().length >= 1) {

                SimpleLiteral identifierLiteral = SimpleLiteral.Factory.parse(abstractRecord.getIdentifierArray(0).getDomNode());
                String recordId = identifierLiteral.getDomNode().getChildNodes().item(0).getChildNodes().item(0).getNodeValue();

                recordIdList.add(recordId);
            }
        }

        log.debug("Found {} record ids based on catalog response with {} matched and {} returned", recordIdList.size(), searchResults.getNumberOfRecordsMatched(),
                searchResults.getNumberOfRecordsReturned());

        VirtGraph graph = new VirtGraph(cons.getUriGraph(), cons.getUrlVirtuosoJdbc(), cons.getVirtuosoUser(), cons.getVirtuosoPass());
        Model model = ModelFactory.createModelForGraph(graph);

        // request detailed description for each record and add to model:
        log.info("Processing {} records into model {}", recordIdList.size(), model);
        IsoToRdfMapper mapper = new IsoToRdfMapper();
        try {
            for (int i = 0; i < recordIdList.size(); i++) {
                log.debug("Processing record number {}/{}", i, maxRecords);

                try {
                    String recordDescription = csw.executeGetRecordsById(recordIdList.get(i));
                    model = mapper.addGetRecordByIdResponseToModel(model, recordDescription);
                } catch (OXFException e) {
                    log.error("Error processing record {}", i, e);
                }
            }

            log.debug("DONE - model: {} | graph: {}", model, graph);
        } catch (RuntimeException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            model.close();
        }
    }

}
