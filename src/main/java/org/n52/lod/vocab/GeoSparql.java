package org.n52.lod.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * TODO:update
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
public class GeoSparql {

    public static final String URI = "http://www.opengis.net/ont/geosparql#";

    public static final String PREFIX = "ogc";

    private static final Model m = ModelFactory.createDefaultModel();
    
    /**
     * TODO:update
     * geo:Point a rdf:Property ; 
     * rdf:datatype="http://www.w3.org/2001/XMLSchema#string" 
     * rdfs:label "wktLiteral"@en ; 
     * rdfs:comment "A Well-known Text serialization of a geometry object."@en
     * .
     */
    public static final Property asWKT = m.createProperty(URI + "asWKT");
}
