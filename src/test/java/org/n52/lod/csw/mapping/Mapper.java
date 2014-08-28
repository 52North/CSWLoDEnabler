package org.n52.lod.csw.mapping;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;

public class Mapper {

    private IsoToRdfMapper mapper;

    @Before
    public void createMapper() {
        this.mapper = new IsoToRdfMapper();
    }

    @Test
    public void mapperMaps() throws Exception {
        String getRecordResp = Resources.toString(Resources.getResource("GetRecordByIdResponse_0.xml"), Charsets.UTF_8);
        GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(getRecordResp, new XmlOptions());

        Model model = this.mapper.createModelFromGetRecordByIdResponse(xb_getRecordByIdResponse);

        Graph graph = model.getGraph();
        System.out.println(model);
        System.out.println(graph);

        // TODO add test assertions
    }

}
