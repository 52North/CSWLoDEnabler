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
package org.n52.lod.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * A vocabulary for describing geographical regions in RDF.
 * 
 * @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
 * @prefix geom: <http://geovocab.org/geometry#> .
 * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
 * 
 * @author Benjamin Pross
 *
 */
public class GeoVocab {

    public static final String URI = "http://geovocab.org/geometry#";

    public static final String PREFIX = "geom";

    private static final Model m = ModelFactory.createDefaultModel();
    
    /**
     * geo:Point a rdf:Property ; 
     * rdfs:comment "Relates a resource to its geometric shape."@en
     * .
     */
    public static final Property geometry = m.createProperty(URI + "geometry");
    
    /**
     * geo:Point a rdf:Property ; 
     * rdfs:Super-class grouping all geometrical representations (also ones in non-RDF formats, such as KML, GML, WKT...)."@en
     * .
     */
    public static final Property geometryType = m.createProperty(URI + "Geometry");
}
