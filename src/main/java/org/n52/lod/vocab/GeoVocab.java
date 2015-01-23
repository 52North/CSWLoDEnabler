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
