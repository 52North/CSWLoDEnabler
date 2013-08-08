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

    public static final Property used = m.createProperty(URI + "used");
    
    public static final Property generated = m.createProperty(URI + "generated");
    
    private PROV()
    {
    }

    public static String getURI() {
        return URI;
    }
}
