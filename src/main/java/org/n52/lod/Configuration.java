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
package org.n52.lod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private String nsGMD;

    private String nsCSW;

    private String urlVirtuosoJdbc;

    private String virtuosoUser;

    private String virtuosoPass;

    private String uriGraph;

    private String urlCSW;

    private String testRecordId;
    
    private boolean addToServer;
    
    private boolean saveToFile;

    private String projectUrl;

    private String projectName;

    private String projectShortname;

    private String uriBase;
    
    private ProgressListener progressListener;

    public static final String DEFAULT_CONFIG_FILE = "/lod.properties";

    public Configuration(String configFile) {
        Properties props = new Properties();
        
        if(configFile == null)
            configFile = DEFAULT_CONFIG_FILE;

        log.info("Loading properties from {}", configFile);
        try(InputStream in = Configuration.class.getResourceAsStream(configFile);) {
                props.load(in);
        } catch (IOException| NullPointerException e) {
            log.error("Could not read properties file {}", configFile, e);
            return;
        }
        
        init(props);
    }
    
    public Configuration(Properties props) {
        
        init(props);
        
        log.info("NEW {}", this);
    }
    
    public void init(Properties props){

        nsGMD = props.getProperty("NS_GMD");
        nsCSW = props.getProperty("NS_CSW");
        urlVirtuosoJdbc = props.getProperty("URL_VIRTUOSO_JDBC");
        virtuosoUser = props.getProperty("VIRTUOSO_USER");
        virtuosoPass = props.getProperty("VIRTUOSO_PASS");
        uriGraph = props.getProperty("URI_GRAPH");
        urlCSW = props.getProperty("URL_CSW");
        testRecordId = props.getProperty("TEST_RECORD_ID");
        saveToFile = Boolean.valueOf(props.getProperty("SAVE_TO_FILE") );
        addToServer = Boolean.valueOf(props.getProperty("ADD_TO_SERVER") );
        
        projectUrl = props.getProperty("PROJECT_URL");
        projectName = props.getProperty("PROJECT_NAME");
        projectShortname = props.getProperty("PROJECT_SHORTNAME");
        uriBase = props.getProperty("URI_BASE");
        
    }

    public String getNsGMD() {
        return nsGMD;
    }

    public String getNsCSW() {
        return nsCSW;
    }

    public String getUrlVirtuosoJdbc() {
        return urlVirtuosoJdbc;
    }

    public String getVirtuosoUser() {
        return virtuosoUser;
    }

    public String getVirtuosoPass() {
        return virtuosoPass;
    }

    public String getUriGraph() {
        return uriGraph;
    }

    public String getUrlCSW() {
        return urlCSW;
    }

    public String getTestRecordId() {
        return testRecordId;
    }

    public boolean isAddToServer() {
        return addToServer;
    }

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectShortname() {
        return projectShortname;
    }

    public String getUriBase() {
        return uriBase;
    }

	public ProgressListener getProgressListener() {
		return progressListener;
	}

	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}

}
