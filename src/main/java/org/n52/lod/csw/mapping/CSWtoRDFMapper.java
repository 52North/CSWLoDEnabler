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
package org.n52.lod.csw.mapping;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.UUID;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;
import net.opengis.gml.BoundingBoxDocument;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.isotc211.x2005.gco.CharacterStringPropertyType;
import org.isotc211.x2005.gmd.AbstractEXGeographicExtentType;
import org.isotc211.x2005.gmd.AbstractMDIdentificationType;
import org.isotc211.x2005.gmd.CIAddressType;
import org.isotc211.x2005.gmd.CICitationType;
import org.isotc211.x2005.gmd.CIContactType;
import org.isotc211.x2005.gmd.CIDatePropertyType;
import org.isotc211.x2005.gmd.CIResponsiblePartyPropertyType;
import org.isotc211.x2005.gmd.CIResponsiblePartyType;
import org.isotc211.x2005.gmd.DQDataQualityType;
import org.isotc211.x2005.gmd.EXExtentPropertyType;
import org.isotc211.x2005.gmd.EXExtentType;
import org.isotc211.x2005.gmd.EXGeographicBoundingBoxType;
import org.isotc211.x2005.gmd.LILineageType;
import org.isotc211.x2005.gmd.LISourceType;
import org.isotc211.x2005.gmd.MDDataIdentificationType;
import org.isotc211.x2005.gmd.MDDistributionPropertyType;
import org.isotc211.x2005.gmd.MDDistributionType;
import org.isotc211.x2005.gmd.MDDistributorType;
import org.isotc211.x2005.gmd.MDFormatType;
import org.isotc211.x2005.gmd.MDIdentificationPropertyType;
import org.isotc211.x2005.gmd.MDIdentifierPropertyType;
import org.isotc211.x2005.gmd.MDIdentifierType;
import org.isotc211.x2005.gmd.MDKeywordsPropertyType;
import org.isotc211.x2005.gmd.MDMetadataDocument;
import org.isotc211.x2005.gmd.MDMetadataType;
import org.isotc211.x2005.gmd.MDReferenceSystemPropertyType;
import org.isotc211.x2005.gmd.MDScopeCodePropertyType;
import org.isotc211.x2005.gmd.MDTopicCategoryCodePropertyType;
import org.isotc211.x2005.gmd.RSIdentifierType;
import org.n52.lod.Configuration;
import org.n52.lod.vocab.BGSSpatial;
import org.n52.lod.vocab.BasicGeo;
import org.n52.lod.vocab.GeoSparql;
import org.n52.lod.vocab.GeoVocab;
import org.n52.lod.vocab.PROV;
import org.n52.oxf.OXFException;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

import fr.ign.eden.xsd.metafor.x20050620.gmi.LEProcessStepDocument;
import fr.ign.eden.xsd.metafor.x20050620.gmi.LEProcessStepType;

/**
 * This class maps CSW records encoded as ISO 19115 to RDF.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>, Daniel Nüst
 */
public class CSWtoRDFMapper implements XmlToRdfMapper {

    private String uriBase_;

    private String uriBase_person;

    private String uriBase_organisation;

    private String uriBase_project;

    private String uriBase_record;

    private String uriBase_types;

    private String uriBase_process;
    
    private String uriBase_geometry;

    private Object projectName_long;

    private Object projectName_short;

    private String project_url;

    private String project_uri;

    private Configuration config;
    
    private boolean isRevision = false;

    private static Logger log = LoggerFactory.getLogger(CSWtoRDFMapper.class);

    public CSWtoRDFMapper(Configuration config, String uriBase, String projectUrl, String projectName, String projectNameShort) {
        this.config = config;

        this.uriBase_ = uriBase;

        uriBase_person = uriBase_ + "person/";
        uriBase_organisation = uriBase_ + "organization/";
        uriBase_project = uriBase_ + "project/";
        uriBase_record = uriBase_ + "record/";
        uriBase_types = uriBase_ + "types/";
        uriBase_process = uriBase_ + "process/";
        uriBase_geometry = uriBase_ + "geometry/";
        projectName_long = projectName;
        projectName_short = projectNameShort;
        project_url = projectUrl;
        project_uri = uriBase_project + projectName_short;

        log.debug("NEW {}", this);
    }

    public CSWtoRDFMapper(Configuration config) {
        this(config, config.getUriBase(), config.getProjectUrl(), config.getProjectName(), config.getProjectShortname());
    }

    @Override
    public Model map(XmlObject xml) throws OXFException, XmlException {
        if (xml instanceof GetRecordByIdResponseDocument) {
            GetRecordByIdResponseDocument doc = (GetRecordByIdResponseDocument) xml;
            
            Model m = ModelFactory.createDefaultModel();
            return addGetRecordByIdResponseToModel(m, doc);
        }
        String s = xml.xmlText().substring(0, Math.min(xml.xmlText().length(), 200));
        log.error("XmlObject not supported by this mapper: {}", s);

        throw new UnsupportedOperationException("XML instance is not supported by this mapper: " + s);
    }

    /**
     * the method that does the actual work
     * 
     * @return the model if everything went fine, otherwise null
     */
    protected Model addGetRecordByIdResponseToModel(Model model, GetRecordByIdResponseDocument xb_getRecordByIdResponse) throws XmlException, OXFException {
        log.debug("Start addGetRecordByIdResponse");
        Node xb_MDMetadataNode = xb_getRecordByIdResponse.getGetRecordByIdResponse().getDomNode().getChildNodes().item(0);

        if (xb_MDMetadataNode == null) {
            log.warn("Could not get first child node from response: {}", xb_getRecordByIdResponse.xmlText());
            return null;
        }

        log.debug("Start parsing...");
        MDMetadataType xb_metadata = MDMetadataDocument.Factory.parse(xb_MDMetadataNode).getMDMetadata();
        String recordId = xb_metadata.getFileIdentifier().getCharacterString();


        /*
         * check also RS_Identifier, as this might be different from the file id.
         */
        String resourceID = recordId;
        try {
             resourceID = createURIStringFromIdentifier(xb_metadata.getIdentificationInfoArray(0).getAbstractMDIdentification().getCitation().getCICitation().getIdentifierArray(0).getMDIdentifier());          
        } catch (Exception e) {
            log.warn("Could not parse RS_Identifier.");
        }
        
        boolean differentIDs = !recordId.equals(resourceID);
        
        /*
         *  create the record resource
         */
        Resource recordResource = model.createResource(uriBase_record + recordId);
        log.debug("Parsing done. Mapping '{}' to resource '{}'", recordId, recordResource);

        recordResource.addProperty(DC_11.identifier, xb_metadata.getFileIdentifier().getCharacterString());
        
        if(differentIDs){
            /*
             * add different id to the record for completeness sake
             */
            recordResource.addProperty(DC_11.identifier, resourceID);
        }
        
        log.trace("Mapping literals for {}", recordResource);
        addLiteral(recordResource, xb_metadata.getLanguage(), DC_11.language);

        log.trace("Mappping scope code {}", recordResource);
        mapScopeCode(xb_metadata, recordResource);

        log.trace("Mapping responsible party for {}", recordResource);
        mapResponsibleParty(model, xb_metadata, recordResource);

        log.trace("Mapping date for {}", recordResource);
        parseDateStamp(xb_metadata, recordResource);

        log.trace("Mapping reference system for {}", recordResource);
        parseReferenceSystem(xb_metadata, recordResource);

        log.trace("Mapping spatial extend for {}", recordResource);        
        parseSpatialExtend(xb_metadata, recordResource, model);
        
        log.trace("Mapping identification for {}", recordResource);
        parseIdentification(model, xb_metadata, recordId, recordResource);

        log.trace("mapping distribution for {}", recordResource);
        MDDistributionPropertyType distributionInfoType = xb_metadata.getDistributionInfo();
        if (distributionInfoType != null) {
            MDDistributionType distributionInfo = distributionInfoType.getMDDistribution();
            parseDistribution(model, recordResource, distributionInfo);
        }

        log.trace("Mapping quality for {}", recordResource);
        if (xb_metadata.getDataQualityInfoArray() != null) {
            parseDataQuality(model, xb_metadata, recordId, recordResource);
        }
        
        if(xb_metadata.getParentIdentifier() != null){           
            Resource parentResource = model.createResource(uriBase_record + xb_metadata.getParentIdentifier().getCharacterString());                           
            if(isRevision){
                recordResource.addProperty(PROV.wasRevisionOf, parentResource);                                            
            }else{
                recordResource.addProperty(PROV.wasDerivedFrom, parentResource);  
            }            
        }

        log.debug("Done mapping '{}'", recordResource);
        return model;
    }

    private void parseSpatialExtend(MDMetadataType xb_metadata,
            Resource recordResource, Model model)
    {
        MDIdentificationPropertyType[] idInfoArray = xb_metadata.getIdentificationInfoArray();
        
        AbstractMDIdentificationType identification = idInfoArray[0].getAbstractMDIdentification();
        
        if(identification instanceof MDDataIdentificationType){
            
            MDDataIdentificationType mdDataIdentificationType = (MDDataIdentificationType)identification;
            
            EXExtentPropertyType[] exExtentPropertyTypes = mdDataIdentificationType.getExtentArray();
            
            for (EXExtentPropertyType exExtentPropertyType : exExtentPropertyTypes) {
                
                EXExtentType exExtentType = exExtentPropertyType.getEXExtent();
                
                if(exExtentType != null &&exExtentType.getGeographicElementArray() != null && exExtentType.getGeographicElementArray().length > 0){
                    
                    AbstractEXGeographicExtentType abstractEXGeographicExtentType = exExtentType.getGeographicElementArray(0).getAbstractEXGeographicExtent();
                    
                    if(abstractEXGeographicExtentType instanceof EXGeographicBoundingBoxType){
                        
                        EXGeographicBoundingBoxType exGeographicBoundingBoxType = (EXGeographicBoundingBoxType)abstractEXGeographicExtentType;
                        
                        double south = exGeographicBoundingBoxType.getSouthBoundLatitude().getDecimal().doubleValue();
                        double east = exGeographicBoundingBoxType.getEastBoundLongitude().getDecimal().doubleValue();
                        double north = exGeographicBoundingBoxType.getNorthBoundLatitude().getDecimal().doubleValue();
                        double west = exGeographicBoundingBoxType.getWestBoundLongitude().getDecimal().doubleValue();
                        
                        //calculate center
                        double centerLon = (east + west) / 2;
                        double centerLat = (north + south) / 2;
                        
                        recordResource.addProperty(BasicGeo.longitude, "" + centerLon);                        
                        recordResource.addProperty(BasicGeo.latitude, "" + centerLat);
                        
                        /*
                         * create geometry object
                         * create property for recordResource
                         */
                        Resource geometryResource = model.createResource(uriBase_geometry + UUID.randomUUID().toString().substring(0, 5));                        
                       
                        geometryResource.addLiteral(GeoSparql.asWKT, model.createTypedLiteral("POINT(" + centerLon + " "  + centerLat + ")", WKTLiteralType.theWKTLiteralType));
                        
                        geometryResource.addProperty(RDF.type, GeoVocab.geometryType);
                        
                        recordResource.addProperty(GeoVocab.geometry, geometryResource);
                        
                        String crs = "";
                        try{
                            crs = recordResource.getProperty(DCTerms.spatial).getString();
                        }catch(Exception e){
                            log.warn("Resource has no reference system property. Saving bounding box without reference system name.");
                        }
                        
                        recordResource.addProperty(BGSSpatial.hasBoundingBox, createGMLBoundingBoxString(south, east, north, west, crs));
                        
                    }
                    
                }                
            }
            
        }
        
    }
    
    private void parseIdentification(Model model,
            MDMetadataType xb_metadata,
            String recordId,
            Resource recordResource) throws OXFException {
        MDIdentificationPropertyType[] idInfoArray = xb_metadata.getIdentificationInfoArray();
        for (int i = 0; i < idInfoArray.length; i++) {
            AbstractMDIdentificationType identification = idInfoArray[i].getAbstractMDIdentification();

            if (identification == null) {
                log.warn("No identification provided for {}, skipping identification {} of {} for: {}", recordId, i, idInfoArray.length, xb_metadata.xmlText());
                continue;
            }

            if (identification.getCitation() != null) {
                parseCitation(recordResource, identification.getCitation().getCICitation());
            }

            // parsing abstract:
            addLiteral(recordResource, identification.getAbstract(), DCTerms.abstract_);
            addLiteral(recordResource, identification.getAbstract(), DC_11.description);

            // parsing pointOfContact:
            if (identification.getPointOfContactArray() != null) {
                CIResponsiblePartyPropertyType[] pointOfContactArray = identification.getPointOfContactArray();
                for (int j = 0; j < pointOfContactArray.length; j++) {
                    CIResponsiblePartyType pointOfContact = pointOfContactArray[j].getCIResponsibleParty();

                    parseResponsibleParty(model, recordResource, pointOfContact);
                }
            }

            // identification.getResourceMaintenanceArray()

            if (identification.getDescriptiveKeywordsArray() != null) {
                MDKeywordsPropertyType[] descriptiveKeywordArray = identification.getDescriptiveKeywordsArray();
                for (int j = 0; j < descriptiveKeywordArray.length; j++) {
                    addLiterals(recordResource, descriptiveKeywordArray[j].getMDKeywords().getKeywordArray(), DC_11.subject);
                }
            }

            // if (identification.getResourceConstraintsArray() != null) {
            // MDConstraintsPropertyType[] resourceConstraintsArray =
            // identification.getResourceConstraintsArray();
            // for (int j = 0; j < resourceConstraintsArray.length; j++) {
            // addLiterals(recordResource,
            // resourceConstraintsArray[j].getMDConstraints().getUseLimitationArray(),
            // DC.rights);
            // }
            // }

            // parse topicCategories:
            XmlObject[] xmlObjectArray = identification.selectChildren(config.getNsGMD(), "topicCategory");
            for (int j = 0; j < xmlObjectArray.length; j++) {
                MDTopicCategoryCodePropertyType topicCategory = (MDTopicCategoryCodePropertyType) xmlObjectArray[j];
                recordResource.addLiteral(DC_11.subject, topicCategory.getMDTopicCategoryCode().toString());
            }
        }
    }

    private void parseReferenceSystem(MDMetadataType xb_metadata,
            Resource recordResource) {
        MDReferenceSystemPropertyType[] referenceArray = xb_metadata.getReferenceSystemInfoArray();
        for (int i = 0; i < referenceArray.length; i++) {
            String refCode = referenceArray[i].getMDReferenceSystem().getReferenceSystemIdentifier().getRSIdentifier().getCode().getCharacterString();
            String refCodeSpace = referenceArray[i].getMDReferenceSystem().getReferenceSystemIdentifier().getRSIdentifier().getCodeSpace().getCharacterString();

            recordResource.addProperty(DCTerms.spatial, refCodeSpace + "::" + refCode);
        }
    }

    private void mapResponsibleParty(Model model,
            MDMetadataType xb_metadata,
            Resource recordResource) throws OXFException {
        CIResponsiblePartyPropertyType[] xb_contactArray = xb_metadata.getContactArray();
        for (int i = 0; i < xb_contactArray.length; i++) {
            CIResponsiblePartyType contact = xb_contactArray[i].getCIResponsibleParty();

            parseResponsibleParty(model, recordResource, contact);
        }
    }

    private void parseDateStamp(MDMetadataType xb_metadata,
            Resource recordResource) {
        if (xb_metadata.getDateStamp() != null) {
            Calendar date = xb_metadata.getDateStamp().getDate();
            if (date != null) {
                recordResource.addProperty(DCTerms.modified, date.toString());
            }
            Calendar dateTime = xb_metadata.getDateStamp().getDateTime();
            if (dateTime != null) {
                recordResource.addProperty(DCTerms.modified, dateTime.toString());
            }
        }
    }

    private void mapScopeCode(MDMetadataType xb_metadata,
            Resource recordResource) {
        MDScopeCodePropertyType[] xb_hierarchyLevelArray = xb_metadata.getHierarchyLevelArray();
        for (int i = 0; i < xb_hierarchyLevelArray.length; i++) {
            String hierarchyLevelCode = xb_hierarchyLevelArray[i].getMDScopeCode().getCodeListValue();
            recordResource.addProperty(DC_11.type, uriBase_types + hierarchyLevelCode);
        }
    }

    /**
     * This means parsing the ISO 19115 lineage and translating it to PROV. See
     * also http://www.w3.org/2001/sw/wiki/images/a/a1/Lineage.owl for a
     * suggested mapping.
     */
    private void parseDataQuality(Model model,
            MDMetadataType xb_metadata,
            String recordId,
            Resource recordResource) throws XmlException, OXFException {
        for (int i = 0; i < xb_metadata.getDataQualityInfoArray().length; i++) {
            DQDataQualityType dataQuality = xb_metadata.getDataQualityInfoArray(i).getDQDataQuality();

            if (dataQuality.getLineage() != null) {
                LILineageType lineage = dataQuality.getLineage().getLILineage();

                // create provenance resource 'processStepResource'
                Resource processStepResource = model.createResource(uriBase_process + recordId);
                processStepResource.addProperty(RDF.type, DCTerms.ProvenanceStatement);
                processStepResource.addProperty(RDF.type, PROV.Activity);

                // associate 'processStepResource' with 'recordResource'
                processStepResource.addProperty(PROV.generated, recordResource); // /
                                                                                 // TODO
                                                                                 // really
                                                                                 // recordResource
                                                                                 // or
                                                                                 // new
                                                                                 // generated
                                                                                 // resource???

                if (lineage.getProcessStepArray() != null) {
                    for (int j = 0; j < lineage.getProcessStepArray().length; j++) {

                        LEProcessStepType processStep = LEProcessStepDocument.Factory.parse(lineage.getProcessStepArray(j).getDomNode().getFirstChild()).getLEProcessStep();

                        if (processStep != null) {

                            addLiteral(processStepResource, processStep.getDescription(), DC_11.description);

                            addLiteral(processStepResource, processStep.getRationale(), DCTerms.abstract_);

                            if (processStep.getDateTime() != null) {
                                processStepResource.addProperty(DCTerms.date, processStep.getDateTime().toString());
                            }

                            if (processStep.getProcessorArray() != null) {
                                for (int h = 0; h < processStep.getProcessorArray().length; h++) {
                                    CIResponsiblePartyPropertyType processorDude = processStep.getProcessorArray(h);
                                    parseResponsibleParty(model, processStepResource, processorDude.getCIResponsibleParty());
                                }
                            }

                            //
                            // parsing source(s) of this dataset:
                            //
                            /*
                             * FIXME sourcearray doesn't seem to be parsed correctly
                             * it is not null, but is empty
                             * workaround: parse domnode
                             */
                            
                            if (processStep.getSourceArray() != null) {
                                for (int h = 0; h < processStep.getSourceArray().length; h++) {
                                    LISourceType source = processStep.getSourceArray(h).getLISource();
                                    
                                    String sourceID = getSourceIdentifierCodespaceCharacterString(source);
                                    
                                    if(sourceID == ""){
                                        sourceID = UUID.randomUUID().toString().substring(0, 4);
                                    }
                                    
                                    sourceID = uriBase_record + sourceID;
                                    
                                    Resource sourceResource = model.createResource(sourceID);
                                    sourceResource.addProperty(RDF.type, PROV.Entity);

                                    addLiteral(sourceResource, source.getDescription(), DC_11.description);

                                    if (source.getSourceCitation() != null) {
                                        parseCitation(recordResource, source.getSourceCitation().getCICitation());
                                    }

                                    // associate 'source' with
                                    // 'processStepResource':
                                    processStepResource.addProperty(PROV.used, sourceResource);
                                }
                            }
                        }

                        //
                        // parsing processingInformation of this
                        // processStep:
                        // <gmi:LE_ProcessStep
                        // xmlns:gmi="http://eden.ign.fr/xsd/metafor/20050620/gmi"
                        // xmlns="http://www.isotc211.org/2005/gmd"
                        // xmlns:gco="http://www.isotc211.org/2005/gco">
                        // ...
                        // <gmi:processingInformation>
                        // <gmi:LE_Processing>
                        // ...

                        // TODO XMLBeans parsing doesn't work! Extremely
                        // wild HACK follows...

                        // LEProcessingPropertyType processingProperty =
                        // LEProcessingPropertyType.Factory.parse(processStep.getDomNode().getLastChild().getLastChild());
                        // if (processingProperty != null) {
                        // LEProcessingType processing =
                        // LEProcessingType.Factory.parse(processingProperty.getDomNode().getFirstChild(),
                        // xmlOptions);
                        //
                        // //assuming identifier on first position
                        // MDIdentifierPropertyType identifier =
                        // MDIdentifierPropertyType.Factory.parse(processing.getDomNode().getFirstChild().getFirstChild().getFirstChild());
                        //
                        // if (identifier != null) {
                        // parseIdentifier(processStepResource,
                        // identifier.getMDIdentifier());
                        // }
                        //
                        // addLiteral(processStepResource,
                        // processing.getProcedureDescription(),
                        // DC.description);
                        //
                        // if (processing.getDocumentationArray() != null) {
                        // for (int k = 0; k <
                        // processing.getDocumentationArray().length; k++) {
                        // parseCitation(processStepResource,
                        // processing.getDocumentationArray(k).getCICitation());
                        // }
                        // }
                        // }
                    }
                }

                recordResource.addProperty(DCTerms.provenance, processStepResource);
            }
        }
    }

    private String getSourceIdentifierCodespaceCharacterString(LISourceType source)
    {
        String result = "";
        
        try {
            result = createURIStringFromIdentifier(source.getSourceCitation().getCICitation().getIdentifierArray(0).getMDIdentifier()); 
        } catch (Exception e) {
               log.warn("Could not get source identifier", e);
        } 
        return result;
    }

    private void parseDistribution(Model model,
            Resource recordResource,
            MDDistributionType distributionInfo) throws OXFException {
        if (distributionInfo != null) {

            for (int i = 0; i < distributionInfo.getDistributionFormatArray().length; i++) {
                MDFormatType format = distributionInfo.getDistributionFormatArray(i).getMDFormat();

                if (format != null) {
                    addLiteral(recordResource, format.getName(), DCTerms.format);
                    addLiteral(recordResource, format.getSpecification(), DCTerms.format);
                }
            }

            for (int i = 0; i < distributionInfo.getDistributorArray().length; i++) {
                MDDistributorType distributor = distributionInfo.getDistributorArray(i).getMDDistributor();

                if (distributor != null) {
                    if (distributor.getDistributorContact() != null) {
                        if (distributor.getDistributorContact().getCIResponsibleParty() != null) {
                            parseResponsibleParty(model, recordResource, distributor.getDistributorContact().getCIResponsibleParty());
                        }
                    }
                }
            }
        }
    }

    private void parseCitation(Resource resource,
            CICitationType citation) throws OXFException {
        // parsing title:
        addLiteral(resource, citation.getTitle(), DC_11.title);

        // tparsing date:
        String date = null;
        if (citation.getDateArray() != null) {
            for (int k = 0; k < citation.getDateArray().length; k++) {
                CIDatePropertyType dateProperty = citation.getDateArray(k);

                if (dateProperty.getCIDate() != null) {
                    if (dateProperty.getCIDate().getDate() != null) {
                        if (dateProperty.getCIDate().getDate().getDate() != null) {
                            date = TimeFactory.createTime(dateProperty.getCIDate().getDate().getDate().toString()).toISO8601Format();
                        } else if (dateProperty.getCIDate().getDate().getDateTime() != null) {
                            date = TimeFactory.createTime(dateProperty.getCIDate().getDate().getDateTime().toString()).toISO8601Format();
                        }
                    }
                    if (date != null) {
                        if (dateProperty.getCIDate().getDateType() != null) {
                            if (dateProperty.getCIDate().getDateType().getCIDateTypeCode() != null) {
                                String dateType = dateProperty.getCIDate().getDateType().getCIDateTypeCode().getCodeListValue();
                                if (dateType != null) {
                                    if (dateType.equals("publication")) {
                                        resource.addProperty(PROV.generatedAtTime, date);
                                    } else if (dateType.equals("creation")) {
                                        resource.addProperty(DCTerms.created, date);
                                    } else if (dateType.equals("revision")) {
                                        isRevision = true;
                                    } else {
                                        throw new OXFException("date type '" + dateType + "' not supported");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

//        // parsing identifiers:
//        if (citation.getIdentifierArray() != null) {
//            MDIdentifierPropertyType[] citationIdArray = citation.getIdentifierArray();
//            for (int j = 0; j < citationIdArray.length; j++) {
//                MDIdentifierType citationId = citationIdArray[j].getMDIdentifier();
//                parseIdentifier(resource, citationId);
//            }
//        }

    }

//    /**
//     * parses an MDIdentifierType element and associates the identifier with the
//     * resource.
//     */
//    private void parseIdentifier(Resource resource,
//            MDIdentifierType identifier) {
//        resource.addProperty(DC_11.identifier, createURIStringFromIdentifier(identifier));
//    }
    
    private String createURIStringFromIdentifier(MDIdentifierType identifier){

        String uri;

        try {
            RSIdentifierType rsIdentifier = (RSIdentifierType) identifier;
            String code = rsIdentifier.getCode().getCharacterString();
            String codeSpace = rsIdentifier.getCodeSpace().getCharacterString();

            // build one identifier URI:
            if (codeSpace.startsWith("urn:")) {
                uri = codeSpace.substring(4) + ":" + code;
            } else {
                uri = codeSpace + ":" + code;
            }
        } catch (ClassCastException e) {
            uri = identifier.getCode().getCharacterString();
        }
        return uri;
    }
    
    /**
     * parses and associates the responsibleParty as a new Resource with the
     * resource.
     * 
     * @return the created Resource for the responsibleParty
     * @throws OXFException
     */
    private Resource parseResponsibleParty(Model model,
            Resource resource,
            CIResponsiblePartyType responsibleParty) throws OXFException {
        if (responsibleParty.getIndividualName() != null) {
            // create resource for individual person contact:
            String name = responsibleParty.getIndividualName().getCharacterString();
            Resource personResource = model.createResource(uriBase_person + name.replace(" ", "-"));
            personResource.addLiteral(FOAF.name, name);
            personResource.addLiteral(VCARD.FN, name);
            personResource.addProperty(RDF.type, FOAF.Person);
            personResource.addProperty(RDF.type, FOAF.Agent);
            personResource.addProperty(RDF.type, DCTerms.Agent);

            // associate with a project:
            Resource gluesProject = model.createResource(project_uri);
            gluesProject.addProperty(RDF.type, FOAF.Project);
            gluesProject.addLiteral(FOAF.name, projectName_long);
            gluesProject.addLiteral(FOAF.name, projectName_short);
            gluesProject.addProperty(FOAF.homepage, project_url);
            gluesProject.addProperty(FOAF.member, personResource);
            personResource.addProperty(FOAF.currentProject, project_uri);

            // read out position name:
            addLiteral(personResource, responsibleParty.getPositionName(), VCARD.ROLE);

            // create resource for organization:
            if (responsibleParty.getOrganisationName() != null) {
                String orgName = responsibleParty.getOrganisationName().getCharacterString();
                Resource orgResource = model.createResource(uriBase_organisation + orgName.replace(" ", "-"));
                orgResource.addLiteral(FOAF.name, orgName);
                orgResource.addProperty(RDF.type, FOAF.Organization);

                orgResource.addProperty(FOAF.member, personResource);
            }

            // read out phone numbers:
            if (responsibleParty.getContactInfo() != null) {
                CIContactType contactInfo = responsibleParty.getContactInfo().getCIContact();

                if (contactInfo.getPhone() != null) {
                    if (contactInfo.getPhone().getCITelephone() != null) {
                        addLiterals(personResource, contactInfo.getPhone().getCITelephone().getVoiceArray(), VCARD.TEL);
                    }
                }

                // read out email address:
                if (contactInfo.getAddress() != null) {
                    CIAddressType addressInfo = contactInfo.getAddress().getCIAddress();

                    if (addressInfo != null) {
                        addLiterals(personResource, addressInfo.getElectronicMailAddressArray(), VCARD.EMAIL);

                        addLiterals(personResource, addressInfo.getDeliveryPointArray(), VCARD.ADR);

                        addLiteral(personResource, addressInfo.getPostalCode(), VCARD.Pcode);
                    }
                }
            }

            // check role of contact:
            if (responsibleParty.getRole() != null) {
                String contactRoleCode = responsibleParty.getRole().getCIRoleCode().getCodeListValue();
                if (contactRoleCode != null && contactRoleCode.equals("publisher")) {
                    resource.addProperty(DC_11.publisher, personResource);
                } else if (contactRoleCode != null && contactRoleCode.equals("distributor")) {
                    resource.addProperty(DC_11.publisher, personResource);
                } else if (contactRoleCode != null && contactRoleCode.equals("pointOfContact")) {
                    resource.addProperty(DC_11.creator, personResource);
                } else if (contactRoleCode != null && contactRoleCode.equals("processor")) {
                    // this 'if' means we are dealing with a provenance
                    // processor ...
                    resource.addProperty(PROV.influencer, personResource);
                    // ... so add further properties:
                    personResource.addProperty(RDF.type, PROV.Person);
                    resource.addProperty(PROV.wasAssociatedWith, personResource);
                } else {
                    log.warn("Unsupported contact role '{}':  ind-name '{}' | orgname '{}'", contactRoleCode, responsibleParty.getIndividualName(), responsibleParty.getOrganisationName());
                    throw new OXFException("Contact role code '" + contactRoleCode + "' not supported.");
                }
            }

            return personResource;
        }

        log.warn("Unsupported contact (non-individual contacts not yet supported) for resource {}: \n{}", resource, responsibleParty.xmlText());
        throw new OXFException("Non-individual contacts not yet supported.");
    }

    /**
     * Adds a String value as a literal to a resource after checking whether the
     * characterStringProperty is null.
     * 
     * @param resource
     *            the {@link Resource} to which a property is added as a literal
     * @param characterStringProperty
     *            the {@link String} value of this
     *            {@link CharacterStringPropertyType} is added to the resource
     * @param property
     *            the {@link Property} which is added to the resource
     * @return true if the literal is added; false otherwise.
     */
    private static boolean addLiteral(Resource resource,
            CharacterStringPropertyType characterStringProperty,
            Property property) {
        if (characterStringProperty != null) {
            resource.addLiteral(property, characterStringProperty.getCharacterString());
            return true;
        }
        return false;
    }

    private String createGMLBoundingBoxString(double south, double east, double north, double west, String crs){
                
        EnvelopeType envelopeType = EnvelopeType.Factory.newInstance();
        
        DirectPositionType lowerCorner = DirectPositionType.Factory.newInstance();
        
        lowerCorner.setStringValue(south + " " + east);
        
        DirectPositionType upperCorner = DirectPositionType.Factory.newInstance();
        
        upperCorner.setStringValue(north + " " + west);
        
        envelopeType.setLowerCorner(lowerCorner);
        envelopeType.setUpperCorner(upperCorner);
        envelopeType.setSrsName(crs);
        
        BoundingBoxDocument boundingBoxDocument = BoundingBoxDocument.Factory.newInstance();
        
        boundingBoxDocument.setBoundingBox(envelopeType);
        
        XmlOptions xmlOptions = new XmlOptions();
        
        xmlOptions.setSaveNoXmlDecl();
        
        StringWriter stringWriter = new StringWriter();    
        
        try {
            boundingBoxDocument.save(stringWriter, xmlOptions);
        } catch (IOException e) {
            log.error("Could not save GML BoundingBoxDocument.", e);
        }
        
        return stringWriter.toString();
        
    }
    
    /**
     * Adds all String values of a characterStringPropertyArray as literals to
     * the resource.
     * 
     * @param resource
     *            the {@link Resource} to which the properties are added as
     *            literals
     * @param characterStringPropertyArray
     *            the {@link String} values of this
     *            {@link CharacterStringPropertyType[]} are added to the
     *            resource
     * @param property
     *            the {@link Property} which is added to the resource
     * @return true if any literal is added; false otherwise.
     */
    private static boolean addLiterals(Resource resource,
            CharacterStringPropertyType[] characterStringPropertyArray,
            Property property) {
        if (characterStringPropertyArray != null) {
            for (int i = 0; i < characterStringPropertyArray.length; i++) {
                resource.addLiteral(property, characterStringPropertyArray[i].getCharacterString());
            }
            return true;
        }
        return false;
    }

    @Override
    public XmlToRdfMapper replicate() {
        return new CSWtoRDFMapper(this.config);
    }
}
