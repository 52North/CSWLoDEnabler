package org.n52.lod;

import java.util.ArrayList;
import java.util.List;

import net.opengis.cat.csw.x202.AbstractRecordType;
import net.opengis.cat.csw.x202.BriefRecordType;
import net.opengis.cat.csw.x202.GetRecordsResponseDocument;
import net.opengis.cat.csw.x202.SearchResultsType;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(CSWLoDEnabler.class);
    
    public static void main(String[] args) throws Exception
    {
        run(2000);
    }
    
    public static void runOverAll () throws Exception {

        long timeStart = System.currentTimeMillis();
        
        int recordsInTotal = 2427;
        int startPos = 1;
        
        while (startPos < recordsInTotal) {
            run (startPos);
            startPos = startPos + 100;
        }
        
        long timeDuration = System.currentTimeMillis() - timeStart;
        LOGGER.info("duration = " + timeDuration);
    }
    
    /**
     * executes the program:
     * 1.) retrieves the record descriptions from the CSW
     * 2.) transforms the descriptions to RDF
     * 3.) inserts the produced RDF into the triplestore
     * @throws Exception 
     */
    private static void run (int startPos) throws Exception {

        CatalogInteractor csw = new CatalogInteractor();
        
        String result = csw.executeGetRecords(10, startPos);
        
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
        
        VirtGraph graph = new VirtGraph(Constants.getInstance().getUriGraph(), Constants.getInstance().getUrlVirtuosoJdbc(), Constants.getInstance().getVirtuosoUser(), Constants.getInstance().getVirtuosoPass());
        Model model = ModelFactory.createModelForGraph(graph);
        
        // request detailed description for each record and add to model:
        LOGGER.info("Processing " + recordIdList.size() + " records.");
        try {
            for (int i = 0; i < recordIdList.size(); i++) {
                LOGGER.info("Processing record number " + i);
                String recordDescription = csw.executeGetRecordsById(recordIdList.get(i));
                model = Iso19115ToRdfMapper.addGetRecordByIdResponseToModel(model, recordDescription);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getLocalizedMessage());
        } finally {
            model.close();
        }
    }
    
}
