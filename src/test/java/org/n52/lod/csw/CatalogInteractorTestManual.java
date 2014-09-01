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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.lod.Configuration;
import org.n52.oxf.util.web.HttpClientException;

public class CatalogInteractorTestManual {

    private static Path tempdir;

    @BeforeClass
    public static void tempdir() throws IOException {
        tempdir = Files.createTempDirectory("csw2lod_");
    }

    private CatalogInteractor interactor;

    @Before
    public void createInteractor() {
        interactor = new CatalogInteractor(new Configuration(Configuration.DEFAULT_CONFIG_FILE));
    }

    @Test
    public void testExecuteGetRecords() throws Exception {
        String result = interactor.executeGetRecords(10, 2000);

        Path file = Files.createFile(tempdir.resolve("catalogResult_GetRecords.xml"));

        Files.write(file, result.getBytes());

        System.out.println("Saved file to " + file);
    }

    @Test
    public void testExecuteGetRecordsById() throws IOException, Exception {
        Configuration config = new Configuration(Configuration.DEFAULT_CONFIG_FILE);
        String result = interactor.executeGetRecordsById(config.getTestRecordId());

        Path file = Files.createFile(tempdir.resolve("catalogResult_GetRecordsById.xml"));
        Files.write(file, result.getBytes());

        System.out.println("Saved file to " + file);
    }

    @Test
    public void countRecordsInCSW() throws IllegalStateException, HttpClientException, IOException, XmlException {
        long numberOfRecords = interactor.getNumberOfRecords();

        System.out.println(numberOfRecords);
    }

}
