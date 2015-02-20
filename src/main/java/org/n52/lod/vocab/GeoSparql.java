package org.n52.lod.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * TODO:update
 * An RDF/OWL vocabulary for representing spatial information
 * 
 * @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
 * @prefix ogc: <http://www.opengis.net/ont/geosparql#> .
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
     * ogc:asWKT a rdf:Property ; 
     * rdf:datatype="http://www.opengis.net/ont/geosparql#wktLiteral" 
     * rdfs:label "Well-known Text Literal"@en ; 
     * rdfs:comment "A Well-known Text serialization of a geometry object."@en
     * .
     */
    public static final Property asWKT = m.createProperty(URI + "asWKT");
}
