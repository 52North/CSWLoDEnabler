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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.n52.lod.Report;
import org.n52.lod.csw.mapping.XmlToRdfMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class FileTripleSink extends AbstractTripleSink {

    private static final Logger log = LoggerFactory.getLogger(FileTripleSink.class);

    private Model model;

    private boolean saved = false;

    private Path rdf;

    private Path turtle;

    public FileTripleSink(XmlToRdfMapper mapper) {
        super(mapper);
        ModelMaker fileModelMaker = ModelFactory.createMemModelMaker(); // createFileModelMaker(tempDir.getAbsolutePath());
        this.model = configureModel(fileModelMaker.createDefaultModel());
        try {
            File f = File.createTempFile("csw2lod_model_", ".rdf");
            this.rdf = Paths.get(f.toURI());
        } catch (IOException e) {
            log.error("Could not create temp file.", e);
            return;
        }
        this.turtle = rdf.resolveSibling(rdf.getFileName().toString().replace("rdf", "ttl"));
    }

    @Override
    public void close() throws Exception {
        long size = model.size();
        model.write(Files.newOutputStreamSupplier(rdf.toFile()).getOutput(), "RDF/XML");
        model.write(Files.newOutputStreamSupplier(turtle.toFile()).getOutput(), "TURTLE");
        model.close();

        this.saved = true;

        log.debug("Saved model in files {} and {}, model size {}", rdf, turtle, size);
    }

    @Override
    public void addRecords(Map<String, GetRecordByIdResponseDocument> records,
            Report report) {
        log.info("Adding {} records...", records.size());
        addRecordsToModel(records, this.model, report);
        log.info("Added records: {}", Arrays.toString(records.keySet().toArray()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileTripleSink [");
        if (model != null && !model.isClosed()) {
            builder.append("model size=");
            builder.append(model.size());
            builder.append(", ");
        }
        builder.append("saved=");
        builder.append(saved);
        builder.append(", ");
        if (rdf != null) {
            builder.append("rdf=");
            builder.append(rdf);
            builder.append(", ");
        }
        if (turtle != null) {
            builder.append("turtle=");
            builder.append(turtle);
        }
        builder.append("]");
        return builder.toString();
    }

}
