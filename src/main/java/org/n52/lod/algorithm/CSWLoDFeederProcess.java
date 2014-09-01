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
package org.n52.lod.algorithm;

import java.io.IOException;

import org.n52.lod.Configuration;
import org.n52.lod.csw.CSWLoDEnabler;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(
        title = "OGC CSW to LOD Triple Store Feeder", version = "0.1")
public class CSWLoDFeederProcess extends CSWLoDEnabler {

    private static final Logger log = LoggerFactory.getLogger(CSWLoDFeederProcess.class);

    private static final String PROCESS_CONFIG_FILE = "/lod.properties";

    public CSWLoDFeederProcess() {
        super(new Configuration(PROCESS_CONFIG_FILE));
    }

    @LiteralDataInput(
            identifier = "addToTripleStore", title = "Insert to triple store", binding = LiteralBooleanBinding.class, abstrakt = "The harvested data is inserted into a pre-configured triplestore",
            defaultValue = "true", minOccurs = 0, maxOccurs = 1)
    public boolean addToTripleStoreInput;

    @LiteralDataInput(
            identifier = "saveToTempfile", title = "Save to tempfile", binding = LiteralBooleanBinding.class, abstrakt = "The data is stored in two temporary files for TURTLE and RDF/XML format",
            defaultValue = "true", minOccurs = 0, maxOccurs = 1)
    public boolean saveToTempfileInput;

    @LiteralDataOutput(
            identifier = "report", title = "Transfer report", binding = LiteralStringBinding.class, abstrakt = "The information collected during the harvesting and inserting as a human readable text")
    public String reportString;

    @Execute
    @Override
    public void runOverAll() throws IOException {
        this.addToServer = addToTripleStoreInput;
        this.saveToFile = saveToTempfileInput;

        log.info("Running {}", this);
        super.runOverAll();

        this.reportString = report.extendedToString();

        log.info("Algorihtm executed.");
    }

}
