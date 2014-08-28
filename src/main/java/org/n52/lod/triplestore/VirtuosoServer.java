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
package org.n52.lod.triplestore;

import java.util.Map;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.n52.lod.Configuration;
import org.n52.lod.Report;
import org.n52.lod.csw.mapping.XmlToRdfMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class VirtuosoServer extends AbstractTripleSink implements TripleSink {

    private static final Logger log = LoggerFactory.getLogger(VirtuosoServer.class);

    private Model model;

    private VirtGraph graph;

    public VirtuosoServer(Configuration config, XmlToRdfMapper mapper) {
        super(mapper);
        this.graph = new VirtGraph(config.getUriGraph(), config.getUrlVirtuosoJdbc(), config.getVirtuosoUser(), config.getVirtuosoPass());

        this.model = configureModel(ModelFactory.createModelForGraph(this.graph));
        log.debug("NEW {}", this);
    }

    @Override
    public void addRecords(Map<String, GetRecordByIdResponseDocument> records,
            Report report) {
        addRecordsToModel(records, model, report);
    }

    @Override
    public void close() throws Exception {
        if (!this.graph.isClosed())
            this.graph.close();

        if (!this.model.isClosed())
            this.model.close();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VirtuosoServer [");
        if (graph != null) {
            builder.append("graph=");
            builder.append(graph);
        }
        builder.append("]");
        return builder.toString();
    }

    public VirtGraph getGraph() {
        return this.graph;
    }

    public Model getModel() {
        return this.model;
    }

}
