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
import org.junit.Before;
import org.junit.Test;
import org.n52.lod.Configuration;

import com.google.common.io.Resources;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class GeometryMapping {

    private CSWtoRDFMapper mapper;

    @Before
    public void createMapper() {
        Configuration c = new Configuration(Configuration.DEFAULT_CONFIG_FILE);
        this.mapper = new GluesMapper(c);
    }

    @Test
    public void mapperMaps() throws Exception {
        
        XmlOptions xmlOptions = new XmlOptions();
        
        xmlOptions.setCharacterEncoding("UTF-8");
        
        xmlOptions.setLoadStripWhitespace();
        
        GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(Resources.getResource("GetRecordByIdResponse_0.xml"), xmlOptions);

        Model model = ModelFactory.createDefaultModel();
        model = this.mapper.addGetRecordByIdResponseToModel(model, xb_getRecordByIdResponse);

        System.out.println(model);

        assertTrue(model.toString().contains("http://data.bgs.ac.uk/ref/Spatial/hasBoundingBox, \"<gml:boundingBox srsName=\"urn:ogc:def:crs:EPSG::4326\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:lowerCorner>-90.0 -180.0</gml:lowerCorner><gml:upperCorner>90.0 180.0</gml:upperCorner></gml:boundingBox>"));
        
    }
}
