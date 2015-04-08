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
package org.n52.lod.csw.mapping;

import static org.junit.Assert.assertTrue;
import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.apache.xmlbeans.XmlOptions;
import org.isotc211.x2005.gmd.MDMetadataDocument;
import org.isotc211.x2005.gmd.MDMetadataType;
import org.junit.Before;
import org.junit.Test;
import org.n52.lod.Configuration;
import org.n52.lod.vocab.PROV;
import org.n52.oxf.OXFException;
import org.w3c.dom.Node;

import com.google.common.io.Resources;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC_11;

public class Mapper {

    private CSWtoRDFMapper mapper;

    private Model model;
    
    private MDMetadataType xb_metadata;
    
    @Before
    public void createMapper() throws Exception, OXFException {
        Configuration c = new Configuration(Configuration.DEFAULT_CONFIG_FILE);
        this.mapper = new GluesMapper(c);
        
        XmlOptions xmlOptions = new XmlOptions();
        
        xmlOptions.setCharacterEncoding("UTF-8");
        
        xmlOptions.setLoadStripWhitespace();
        
        GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(Resources.getResource("GetRecordByIdResponse_0.xml"), xmlOptions);

        model = ModelFactory.createDefaultModel();
        model = this.mapper.addGetRecordByIdResponseToModel(model, xb_getRecordByIdResponse);
        
        Node xb_MDMetadataNode = xb_getRecordByIdResponse.getGetRecordByIdResponse().getDomNode().getChildNodes().item(0);

        xb_metadata = MDMetadataDocument.Factory.parse(xb_MDMetadataNode).getMDMetadata();
        
    }

    @Test
    public void mapperMaps() throws Exception {

        Graph graph = model.getGraph();
        System.out.println(model);
        System.out.println(graph);

        // TODO add test assertions
    }
    
    @Test
    public void mapLanguage(){
        String recordId = xb_metadata.getFileIdentifier().getCharacterString();
        
        assertTrue(model.getProperty(model.getResource(mapper.getUriBase_record() +  recordId), DC_11.language).getResource().getURI().toString().equals("http://rdfdata.eionet.europa.eu/page/eea/languages/en"));
        
    }
    
    @Test
    public void mapProvenance(){
        String recordId = xb_metadata.getFileIdentifier().getCharacterString();
        
        String personName = "Marcel-Adenaeuer";
        
        assertTrue(model.getProperty(model.getResource(mapper.getUriBase_record() +  recordId), org.n52.lod.vocab.PROV.wasDerivedFrom).getResource().getURI().toString().contains("glues:ilr:metadata:dataset:capri"));
        assertTrue(model.getProperty(model.getResource(mapper.getUriBase_record() +  recordId), org.n52.lod.vocab.PROV.wasAttributedTo).getResource().getURI().toString().contains(personName));
        
        Resource provenance = model.getProperty(model.getResource(mapper.getUriBase_record() +  recordId), com.hp.hpl.jena.vocabulary.DCTerms.provenance).getResource();
        
        String sourceURI = provenance.getProperty(PROV.used).getObject().asResource().getURI().toString();
        
        assertTrue(sourceURI.contains("glues-ext:oecd:metadata:dataset:oecdfaoagriculturaloutlook209-2018") || 
                sourceURI.contains("glues-ext:eurostat:metadata:dataset:eurostatdatasetdomains") || 
                sourceURI.contains("glues-ext:fao:metadata:dataset:faostatdatabasedomains"));
        
        assertTrue(provenance.getProperty(PROV.generated).getResource().getURI().toString().contains(recordId));
        assertTrue(provenance.getProperty(PROV.wasAssociatedWith).getResource().getURI().toString().contains(personName));
        assertTrue(provenance.getProperty(PROV.influencer).getResource().getURI().toString().contains(personName));
        
    }

}
