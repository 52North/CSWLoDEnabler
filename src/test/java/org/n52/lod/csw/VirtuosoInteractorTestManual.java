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

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.apache.xmlbeans.XmlOptions;
import org.junit.Test;
import org.n52.lod.Configuration;
import org.n52.lod.csw.mapping.IsoToRdfMapper;
import org.n52.lod.triplestore.VirtuosoServer;

import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class VirtuosoInteractorTestManual {

    @Test
    public void testLoadVirtuoso() throws Exception {
        try (VirtuosoServer server = new VirtuosoServer(Configuration.INSTANCE);) {
            String recordDescription = new CatalogInteractor().executeGetRecordsById(Configuration.INSTANCE.getTestRecordId());
            GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(recordDescription, new XmlOptions());

            IsoToRdfMapper mapper = new IsoToRdfMapper();
            Model model = mapper.addGetRecordByIdResponseToModel(server.getModel(), xb_getRecordByIdResponse);
            System.out.println(model);
        }
    }

    @Test
    public void testQueryVirtuoso() throws Exception {
        /* STEP 1 */
        try (VirtuosoServer server = new VirtuosoServer(Configuration.INSTANCE);) {

            /* STEP 2: Select all data in virtuoso */
            Query sparql = QueryFactory.create("SELECT * WHERE { ?s ?p ?o } limit 100");

            /* STEP 4 */
            try (VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, server.getGraph());) {
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
    }
}
