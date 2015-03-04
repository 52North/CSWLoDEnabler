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
/**
 * 
 */
package org.n52.lod.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Reflecting a subset of the PROV (provenance) Ontology described at:
 * http://www.w3.org/TR/prov-o/
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class PROV {

    public static final String URI = "http://www.w3.org/ns/prov#";

    public static final String PREFIX = "prov";

    private static final Model m = ModelFactory.createDefaultModel();

    public static final Resource Entity = m.createResource(URI + "Entity");

    public static final Resource Activity = m.createResource(URI + "Activity");

    public static final Resource Agent = m.createResource(URI + "Agent");
    
    public static final Resource Person = m.createResource(URI + "Person");

    public static final Property influencer = m.createProperty(URI + "influencer");
    
    public static final Property wasAssociatedWith = m.createProperty(URI + "wasAssociatedWith");
    
    public static final Property wasAttributedTo = m.createProperty(URI + "wasAttributedTo");

    public static final Property used = m.createProperty(URI + "used");
    
    public static final Property generated = m.createProperty(URI + "generated");
    
    public static final Property wasGeneratedBy = m.createProperty(URI + "wasGeneratedBy");
    
    public static final Property wasDerivedFrom = m.createProperty(URI + "wasDerivedFrom");
    
    public static final Property hadPrimarySource = m.createProperty(URI + "hadPrimarySource");
    
    public static final Property wasPrimarySourceOf = m.createProperty(URI + "wasPrimarySourceOf");
    
    public static final Property wasRevisionOf = m.createProperty(URI + "wasRevisionOf");
    
    public static final Property generatedAtTime = m.createProperty(URI + "generatedAtTime");
    
    
    private PROV()
    {
    }

    public static String getURI() {
        return URI;
    }
}
