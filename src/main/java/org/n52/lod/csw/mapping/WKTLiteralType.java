package org.n52.lod.csw.mapping;

import org.n52.lod.vocab.GeoSparql;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;

public class WKTLiteralType extends BaseDatatype {
    
    public static final String theTypeURI = GeoSparql.URI + "wktLiteral";
    public static final WKTLiteralType theWKTLiteralType = new WKTLiteralType();
    
    /** private constructor - single global instance */
    private WKTLiteralType() {
        super(theTypeURI);
    }
    
    @Override
    public String unparse(Object value)
    {
        return value.toString();
    }
    

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException
    {
        return lexicalForm;
    }
    
}