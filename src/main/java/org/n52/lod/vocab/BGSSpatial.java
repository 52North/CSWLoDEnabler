package org.n52.lod.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Reflecting the "Location, bounding box, point" concept described at:
 * http://data.bgs.ac.uk/ref/Spatial
 * 
 * @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
 * @prefix bgsrs: <http://data.bgs.ac.uk/ref/Spatial/> .
 * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
 * 
 * @author Benjamin Pross
 *
 */
public class BGSSpatial {

    public static final String URI = "http://data.bgs.ac.uk/ref/Spatial/";

    public static final String PREFIX = "bgsrs";

    private static final Model m = ModelFactory.createDefaultModel();

    /**
     * bgsrs:hasPointLocation a rdf:Property ; 
     * rdfs:range rdf:XMLLiteral ;
     * rdfs:label "A single point location"@en ; 
     * rdfs:comment "This property gives the location of its subject approximated as a single representative point. Should be expressed in GML."@en
     * .
     */
    public static final Property hasPointLocation = m.createProperty(URI + "hasPointLocation");

    /**
     * 
     * bgsrs:hasBoundingBox a rdf:Property ; 
     * rdfs:range rdf:XMLLiteral ;
     * rdfs:label "Bounding Box"@en ; 
     * rdfs:comment "This property gives a bounding box for the location of the subject. Should be expressed in GML."@en .
     */
    public static final Property hasBoundingBox = m.createProperty(URI + "hasBoundingBox");

    /**
     * bgsrs:hasFootprint a rdf:Property ; 
     * rdfs:range rdf:XMLLiteral ;
     * rdfs:label "Footprint"@en ; 
     * rdfs:comment "This property gives the 2D spatial footprint of the subject. Should be expressed in GML."@en .
     */
    public static final Property hasFootprint = m.createProperty(URI + "hasFootprint");

    /**
     * bgsrs:hasLocationDescription a rdf:Property ; 
     * rdfs:range rdfs:Literal ;
     * rdfs:label "Location Description"@en ;
     * rdfs:comment "Property gives a textual description of where something is located."@en .
     */
    public static final Property hasLocationDescription = m.createProperty(URI + "hasLocationDescription");

    private BGSSpatial() {
    }

    public static String getURI()
    {
        return URI;
    }

}
