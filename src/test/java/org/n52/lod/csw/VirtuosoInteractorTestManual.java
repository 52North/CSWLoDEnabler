package org.n52.lod.csw;

import java.io.IOException;

import org.n52.lod.csw.CatalogInteractor;
import org.n52.lod.csw.Constants;
import org.n52.lod.csw.mapping.IsoToRdfMapper;

import junit.framework.TestCase;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class VirtuosoInteractorTestManual extends TestCase {
    
    public void testLoadVirtuoso() throws IOException
    {

        VirtGraph graph = new VirtGraph(Constants.getInstance().getUriGraph(), Constants.getInstance().getUrlVirtuosoJdbc(), Constants.getInstance().getVirtuosoUser(), Constants.getInstance().getVirtuosoPass());

        Model model = ModelFactory.createModelForGraph(graph);
        
        try {
            String recordDescription = new CatalogInteractor().executeGetRecordsById(Constants.getInstance().getTestRecordId());

            model = IsoToRdfMapper.addGetRecordByIdResponseToModel(model, recordDescription);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            model.close();
        }
    }

    public void testQueryVirtuoso() throws IOException
    {

        /* STEP 1 */
        VirtGraph set = new VirtGraph(Constants.getInstance().getUriGraph(), Constants.getInstance().getUrlVirtuosoJdbc(), Constants.getInstance().getVirtuosoUser(), Constants.getInstance().getVirtuosoPass());

        /* STEP 2 */
        /* Select all data in virtuoso */
        Query sparql = QueryFactory.create("SELECT * WHERE { ?s ?p ?o } limit 100");

        /* STEP 4 */
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, set);

        ResultSet results = vqe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.nextSolution();
            RDFNode graph = result.get("graph");
            RDFNode s = result.get("s");
            RDFNode p = result.get("p");
            RDFNode o = result.get("o");
            System.out.println(graph + " { " + s + " " + p + " " + o + " . }");
        }
    }
}
