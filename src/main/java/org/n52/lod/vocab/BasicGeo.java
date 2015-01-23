package org.n52.lod.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * WGS84 Geo Positioning: an RDF vocabulary
 * 
 * A vocabulary for representing latitude, longitude and 
 * altitude information in the WGS84 geodetic reference datum.
 * 
 * @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
 * @prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> .
 * @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
 * 
 * @author Benjamin Pross
 *
 */
public class BasicGeo {

    public static final String URI = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String PREFIX = "geo";

    private static final Model m = ModelFactory.createDefaultModel();
    
    /**
     * geo:Point a rdf:Property ; 
     * rdfs:subClassOf rdf:resource="http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing" 
     * rdfs:label "point"@en ; 
     * rdfs:comment "A point, typically described using a coordinate system relative to Earth, such as WGS84"@en
     * .
     * rdfs:comment "Uniquely identified by lat/long/alt. i.e.

       spaciallyIntersects(P1, P2) :- lat(P1, LAT), long(P1, LONG), alt(P1, ALT),
         lat(P2, LAT), long(P2, LONG), alt(P2, ALT).

       sameThing(P1, P2) :- type(P1, Point), type(P2, Point), spaciallyIntersects(P1, P2)."@en
     * .
     */
    public static final Property point = m.createProperty(URI + "Point");
    
    /**
     * geo:latitude a rdf:Property ; 
     * rdfs:domain rdf:resource="http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing" 
     * rdfs:label "latitude"@en ; 
     * rdfs:comment "The WGS84 longitude of a SpatialThing (decimal degrees)."@en
     * .
     */
    public static final Property latitude = m.createProperty(URI + "lat");
    
    /**
     * geo:Point a rdf:Property ; 
     * rdfs:domain rdf:resource="http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing" 
     * rdfs:label "longitude"@en ; 
     * rdfs:comment "The WGS84 longitude of a SpatialThing (decimal degrees)."@en
     * .
     */
    public static final Property longitude = m.createProperty(URI + "long");
}
