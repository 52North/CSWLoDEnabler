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
package org.n52.lod.triplestore;

import java.util.Map;
import java.util.Map.Entry;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.n52.lod.Report;
import org.n52.lod.csw.mapping.XmlToRdfMapper;
import org.n52.lod.vocab.PROV;
import org.n52.oxf.OXFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

/**
 * 
 * @author Daniel Nüst
 *
 */
public abstract class AbstractTripleSink implements TripleSink {

    private static final Logger log = LoggerFactory.getLogger(AbstractTripleSink.class);
    
    private XmlToRdfMapper mapper;
    
    public AbstractTripleSink(XmlToRdfMapper mapper) {
        this.mapper = mapper;
    }

    protected void addRecordsToModel(Map<String, GetRecordByIdResponseDocument> records,
            Model m,
            Report report) {
        
        int addedCounter = 0;

        Model result = m;
        for (Entry<String, GetRecordByIdResponseDocument> entry : records.entrySet()) {
            log.debug("Adding {} to the model", entry.getKey());

            try {
                result = mapper.map(m, entry.getValue());
                if (result != null) {
                    addedCounter++;
                    report.added++;
                    report.addedIds.add(entry.getKey());
                } else {
                    report.issues.put(entry.getKey(), "Error while adding to model: " + entry.getValue().xmlText());
                    result = m;
                }
            } catch (OXFException | XmlException e) {
                log.error("Error processing record {}", entry.getKey(), e);
                report.issues.put(entry.getKey(), e);
            }
        }

        log.info("Added {} of {} records to model {}, which now has size {}", addedCounter, records.size(), m.getClass(), m.size());
    }

    protected static Model configureModel(Model model) {
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("dc", DC_11.getURI());
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("vcard", VCARD.getURI());
        model.setNsPrefix("prov", PROV.getURI());
        return model;
    }

}
