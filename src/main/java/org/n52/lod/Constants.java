package org.n52.lod;

import java.io.IOException;
import java.util.Properties;

public class Constants {

    private String nsGMD;
    private String nsCSW;
    private String urlVirtuosoJdbc;
    private String virtuosoUser;
    private String virtuosoPass;
    private String uriGraph;
    private String urlCSW;    
    private String testRecordId;

    
    private static Constants instance = null;

    public static Constants getInstance() throws IOException
    {
        if (instance == null) {
            instance = new Constants();
        }
        return instance;
    }
    
    private Constants () throws IOException {
        Properties props = new Properties();
        
        props.load(Constants.class.getResourceAsStream("/lod.properties"));
        
        nsGMD = props.getProperty("NS_GMD");
        nsCSW = props.getProperty("NS_CSW");
        urlVirtuosoJdbc = props.getProperty("URL_VIRTUOSO_JDBC");
        virtuosoUser = props.getProperty("VIRTUOSO_USER");
        virtuosoPass = props.getProperty("VIRTUOSO_PASS");
        uriGraph = props.getProperty("URI_GRAPH");
        urlCSW = props.getProperty("URL_CSW");
        testRecordId = props.getProperty("TEST_RECORD_ID");
        
    }

    public String getNsGMD()
    {
        return nsGMD;
    }

    public String getNsCSW()
    {
        return nsCSW;
    }

    public String getUrlVirtuosoJdbc()
    {
        return urlVirtuosoJdbc;
    }

    public String getVirtuosoUser()
    {
        return virtuosoUser;
    }

    public String getVirtuosoPass()
    {
        return virtuosoPass;
    }

    public String getUriGraph()
    {
        return uriGraph;
    }

    public String getUrlCSW()
    {
        return urlCSW;
    }

    public String getTestRecordId()
    {
        return testRecordId;
    }
    
    
    
}
