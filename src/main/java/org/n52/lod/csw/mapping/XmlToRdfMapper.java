package org.n52.lod.csw.mapping;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.OXFException;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * 
 * @author Daniel Nüst
 *
 */
public interface XmlToRdfMapper {
    
    public abstract Model map(Model m, XmlObject xml) throws OXFException, XmlException;

}
