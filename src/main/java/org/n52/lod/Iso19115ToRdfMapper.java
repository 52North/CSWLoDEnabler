package org.n52.lod;

import java.util.Calendar;

import net.opengis.cat.csw.x202.GetRecordByIdResponseDocument;

import org.apache.xmlbeans.XmlObject;
import org.isotc211.x2005.gco.CharacterStringPropertyType;
import org.isotc211.x2005.gmd.AbstractMDIdentificationType;
import org.isotc211.x2005.gmd.CIAddressType;
import org.isotc211.x2005.gmd.CICitationType;
import org.isotc211.x2005.gmd.CIContactType;
import org.isotc211.x2005.gmd.CIDatePropertyType;
import org.isotc211.x2005.gmd.CIResponsiblePartyPropertyType;
import org.isotc211.x2005.gmd.CIResponsiblePartyType;
import org.isotc211.x2005.gmd.DQDataQualityType;
import org.isotc211.x2005.gmd.LILineageType;
import org.isotc211.x2005.gmd.LIProcessStepType;
import org.isotc211.x2005.gmd.LISourceType;
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
import org.n52.lod.vocab.PROV;
import org.n52.oxf.OXFException;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.VCARD;

public class Iso19115ToRdfMapper {
    
    public static final String URI_BASE = "http://glues.52north.org/resource/";
    public static final String URI_BASE_PERSONS = URI_BASE + "person/";
    public static final String URI_BASE_ORGANIZATIONS = URI_BASE + "organization/";
    public static final String URI_BASE_PROJECTS = URI_BASE + "project/";
    public static final String URI_BASE_RECORDS = URI_BASE + "record/";
    public static final String URI_BASE_TYPES = URI_BASE + "types/";
    
    public static final String URI_GLUES_PROJECT = URI_BASE_PROJECTS + "GLUES"; // identifies the GLUES project
    public static final String GLUES_PROJECT_NAME_LONG = "Global Assessment of Land Use Dynamics, Greenhouse Gas Emissions and Ecosystem Services";
    public static final String GLUES_PROJECT_NAME_SHORT = "GLUES";
    public static final String GLUES_PROJECT_URL = "http://modul-a.nachhaltiges-landmanagement.de/en/scientific-coordination-glues/";
    
    public static Model createModelFromGetRecordByIdResponse(String getRecordByIdResponse) throws Exception
    { 
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        
        return addGetRecordByIdResponseToModel(model, getRecordByIdResponse);
    }

    public static Model addGetRecordByIdResponseToModel(Model model, String getRecordByIdResponse) throws Exception
    {
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("dc", DC.getURI());
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("vcard", VCARD.getURI());
        model.setNsPrefix("prov", PROV.getURI());
        
        //
        // start reading GetRecordById response:
        //
        GetRecordByIdResponseDocument xb_getRecordByIdResponse = GetRecordByIdResponseDocument.Factory.parse(getRecordByIdResponse);
        Node xb_MDMetadataNode = xb_getRecordByIdResponse.getGetRecordByIdResponse().getDomNode().getChildNodes().item(0);
        
        MDMetadataType xb_metadata = MDMetadataDocument.Factory.parse(xb_MDMetadataNode).getMDMetadata();
        String recordId = xb_metadata.getFileIdentifier().getCharacterString();
        
        // create the record resource
        Resource recordResource = model.createResource(URI_BASE_RECORDS + recordId);
        
        //
        // start adding RDF to model:
        //
        addLiteral(recordResource, xb_metadata.getFileIdentifier(), DC.identifier);
        addLiteral(recordResource, xb_metadata.getParentIdentifier(), DC.source);
        addLiteral(recordResource, xb_metadata.getLanguage(), DC.language);
        
        MDScopeCodePropertyType[] xb_hierarchyLevelArray = xb_metadata.getHierarchyLevelArray();
        for (int i = 0; i < xb_hierarchyLevelArray.length; i++) {
            String hierarchyLevelCode = xb_hierarchyLevelArray[i].getMDScopeCode().getCodeListValue();
            recordResource.addProperty(DC.type, URI_BASE_TYPES + hierarchyLevelCode);
        }
        
        //CharacterStringPropertyType[] xb_hierarchyLevelNameArray = xb_metadata.getHierarchyLevelNameArray();
        
        CIResponsiblePartyPropertyType[] xb_contactArray = xb_metadata.getContactArray();
        for (int i = 0; i < xb_contactArray.length; i++) {
            CIResponsiblePartyType contact = xb_contactArray[i].getCIResponsibleParty();
            
            parseResponsibleParty(model, recordResource, contact);
        }
        
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
        
        MDReferenceSystemPropertyType[] referenceArray = xb_metadata.getReferenceSystemInfoArray();
        for (int i = 0; i < referenceArray.length; i++) {
            String refCode = referenceArray[i].getMDReferenceSystem().getReferenceSystemIdentifier().getRSIdentifier().getCode().getCharacterString();
            String refCodeSpace = referenceArray[i].getMDReferenceSystem().getReferenceSystemIdentifier().getRSIdentifier().getCodeSpace().getCharacterString();
            
            recordResource.addProperty(DCTerms.spatial, refCodeSpace + "::" + refCode);
        }
        
        //
        // Parse Identification Info:
        //
        MDIdentificationPropertyType[] idInfoArray = xb_metadata.getIdentificationInfoArray();
        for (int i = 0; i < idInfoArray.length; i++) {
            AbstractMDIdentificationType identification = idInfoArray[i].getAbstractMDIdentification();
            
            if (identification.getCitation() != null) {
                CICitationType citation = identification.getCitation().getCICitation();
                
                // parsing title:
                addLiteral(recordResource, citation.getTitle(), DC.title);
                
                // parsing date:
                CIDatePropertyType[] dateArray = citation.getDateArray();
                for (int j = 0; j < dateArray.length; j++) {
                    String dateType = dateArray[j].getCIDate().getDateType().getCIDateTypeCode().getCodeListValue();
                    
                    if (dateType.equals("creation")) {
                        Calendar creationDate = dateArray[j].getCIDate().getDate().getDate();
                        if (creationDate != null) {
                            recordResource.addProperty(DCTerms.created, creationDate.toString());
                        }
                        Calendar dateTime = dateArray[j].getCIDate().getDate().getDateTime();
                        if (dateTime != null) {
                            recordResource.addProperty(DCTerms.created, dateTime.toString());
                        }
                    }
                    else {
                        throw new OXFException("date type '" + dateType + "' not supported");
                    }
                }
                
                // parsing identifiers:
                if (citation.getIdentifierArray() != null) {
                    MDIdentifierPropertyType[] citationIdArray = citation.getIdentifierArray();
                    for (int j = 0; j < citationIdArray.length; j++) {
                        MDIdentifierType citationId = citationIdArray[j].getMDIdentifier();
                        parseIdentifier(recordResource, citationId);
                    }
                }
            }
            
            // parsing abstract:
            addLiteral(recordResource, identification.getAbstract(), DCTerms.abstract_);
            addLiteral(recordResource, identification.getAbstract(), DC.description);
            
            // parsing pointOfContact:
            if (identification.getPointOfContactArray() != null) {
                CIResponsiblePartyPropertyType[] pointOfContactArray = identification.getPointOfContactArray();
                for (int j = 0; j < pointOfContactArray.length; j++) {
                    CIResponsiblePartyType pointOfContact = pointOfContactArray[j].getCIResponsibleParty();

                    parseResponsibleParty(model, recordResource, pointOfContact);
                }
            }
            
            //identification.getResourceMaintenanceArray()
            
            if (identification.getDescriptiveKeywordsArray() != null) {
                MDKeywordsPropertyType[] descriptiveKeywordArray = identification.getDescriptiveKeywordsArray();
                for (int j = 0; j < descriptiveKeywordArray.length; j++) {
                    addLiterals(recordResource, descriptiveKeywordArray[j].getMDKeywords().getKeywordArray(), DC.subject);
                }
            }
            
//            if (identification.getResourceConstraintsArray() != null) {
//                MDConstraintsPropertyType[] resourceConstraintsArray = identification.getResourceConstraintsArray();
//                for (int j = 0; j < resourceConstraintsArray.length; j++) {
//                    addLiterals(recordResource, resourceConstraintsArray[j].getMDConstraints().getUseLimitationArray(), DC.rights);
//                }
//            }
            
            
            // parse topicCategories:
            XmlObject[] xmlObjectArray = identification.selectChildren(Constants.getInstance().getNsGMD(), "topicCategory");
            for (int j = 0; j < xmlObjectArray.length; j++) {
                MDTopicCategoryCodePropertyType topicCategory = (MDTopicCategoryCodePropertyType) xmlObjectArray[j];
                recordResource.addLiteral(DC.subject, topicCategory.getMDTopicCategoryCode().toString());
            }
        }
        
        //
        // Parse Distribution Info:
        //
        MDDistributionPropertyType distributionInfoType = xb_metadata.getDistributionInfo();
        if (distributionInfoType != null) {
            MDDistributionType distributionInfo = distributionInfoType.getMDDistribution();
            
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
        
        //
        // Parse Data Quality Info:
        // 
        // This means parsing the ISO 19115 lineage and translating it to PROV.
        // See also http://www.w3.org/2001/sw/wiki/images/a/a1/Lineage.owl for a suggested mapping
        //
        if (xb_metadata.getDataQualityInfoArray() != null) {
            
            for (int i=0; i < xb_metadata.getDataQualityInfoArray().length; i++) {
                DQDataQualityType dataQuality = xb_metadata.getDataQualityInfoArray(i).getDQDataQuality();
                
                if (dataQuality.getLineage() != null) {
                    LILineageType lineage = dataQuality.getLineage().getLILineage();
                    
                    // create provenance resource 'processStepResource'
                    Resource processStepResource = model.createResource(DCTerms.ProvenanceStatement);
                    processStepResource.addProperty(RDF.type, PROV.Activity);
                    
                    // associate 'processStepResource' with 'recordResource'
                    processStepResource.addProperty(PROV.generated, recordResource); /// TODO really recordResource or new generated resource???
                    
                    if (lineage.getProcessStepArray() != null) {
                        for (int j=0; j < lineage.getProcessStepArray().length; j++) {
                            LIProcessStepType processStep = lineage.getProcessStepArray(j).getLIProcessStep();
                            
                            if (processStep != null) {
                                
                                addLiteral(processStepResource, processStep.getDescription(), DC.description);
                                
                                addLiteral(processStepResource, processStep.getRationale(), DCTerms.abstract_);
                                
                                if (processStep.getDateTime() != null) {
                                    processStepResource.addProperty(DCTerms.date, processStep.getDateTime().toString());
                                }
                                
                                if (processStep.getProcessorArray() != null) {
                                    for (int h=0; h < processStep.getProcessorArray().length; h++) {
                                        CIResponsiblePartyPropertyType processorDude = processStep.getProcessorArray(h);
                                        parseResponsibleParty(model, processStepResource, processorDude.getCIResponsibleParty());
                                    }
                                }
                                
                                //
                                // parsing source(s) of this dataset:
                                //
                                if (processStep.getSourceArray() != null) {
                                    for (int h=0; h < processStep.getSourceArray().length; h++) {
                                        LISourceType source = processStep.getSourceArray(h).getLISource();
                                        
                                        Resource sourceResource = model.createResource();
                                        sourceResource.addProperty(RDF.type, PROV.Entity);
                                        
                                        addLiteral(sourceResource, source.getDescription(), DC.description);
                                        
                                        if (source.getSourceCitation() != null) {
                                            
                                            CICitationType sourceCitation = source.getSourceCitation().getCICitation();
                                            addLiteral(sourceResource, sourceCitation.getTitle(), DCTerms.title);
                                            
                                            // trying to parse date:
                                            String date = null;
                                            if (sourceCitation.getDateArray() != null) {
                                                for (int k=0; k < sourceCitation.getDateArray().length; k++) {
                                                    CIDatePropertyType dateProperty = sourceCitation.getDateArray(k);
                                                    
                                                    if (dateProperty.getCIDate() != null) {
                                                        if (dateProperty.getCIDate().getDate() != null) {
                                                            if (dateProperty.getCIDate().getDate().getDate() != null) {
                                                                date = TimeFactory.createTime(dateProperty.getCIDate().getDate().getDate().toString()).toISO8601Format();
                                                            }
                                                            else if (dateProperty.getCIDate().getDate().getDateTime() != null) {
                                                                date = TimeFactory.createTime(dateProperty.getCIDate().getDate().getDateTime().toString()).toISO8601Format();
                                                            }
                                                        }
                                                        if (date != null) {
                                                            if (dateProperty.getCIDate().getDateType() != null) {
                                                                if (dateProperty.getCIDate().getDateType().getCIDateTypeCode() != null) {
                                                                    String dateType = dateProperty.getCIDate().getDateType().getCIDateTypeCode().getCodeListValue();
                                                                    if (dateType != null) {
                                                                        if (dateType.equals("publication")) {
                                                                            sourceResource.addProperty(PROV.generatedAtTime, date);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            if (sourceCitation.getIdentifierArray() != null) {
                                                for (int k=0; k < sourceCitation.getIdentifierArray().length; k++) {
                                                    MDIdentifierType sourceIdentifier = sourceCitation.getIdentifierArray(k).getMDIdentifier();
    
                                                    parseIdentifier(sourceResource, sourceIdentifier);
                                                }
                                            }
                                        }
                                        
                                        // associate 'source' with 'processStepResource':
                                        processStepResource.addProperty(PROV.used, sourceResource);
                                    }
                                }
                            }
                            
                            //
                            // parsing processingInformation of this processStep:
                            //
                            // TODO <gmi:procedureDescription> cannot be parsed with XML Beans.
                        }
                    }
                    
                    recordResource.addProperty(DCTerms.provenance, processStepResource);
                }
            }
            
        }
        
        return model;
    }
    
    /**
     * parses an MDIdentifierType element and associates the identifier with the resource.
     */
    private static void parseIdentifier(Resource resource,
            MDIdentifierType identifier)
    {
        String uri;
        
        try {
            RSIdentifierType rsIdentifier = (RSIdentifierType) identifier;
            String code = rsIdentifier.getCode().getCharacterString();
            String codeSpace = rsIdentifier.getCodeSpace().getCharacterString();
            
            // build one identifier URI:
            if (codeSpace.startsWith("urn:")) {
                uri = codeSpace.substring(4) + ":" + code;
            }
            else {
                uri = codeSpace + ":" + code;
            }
        }
        catch (ClassCastException e) {
            uri = identifier.getCode().getCharacterString();
        }
        
        resource.addProperty(DC.identifier, uri);
    }

    /**
     * parses and associates the responsibleParty as a new Resource with the ownerResource.
     * @return the created Resource for the responsibleParty
     */
    private static Resource parseResponsibleParty(
            Model model,
            Resource ownerResource,
            CIResponsiblePartyType responsibleParty) throws OXFException
    {
        if (responsibleParty.getIndividualName() != null) {
            // create resource for individual person contact:
            String name = responsibleParty.getIndividualName().getCharacterString();
            Resource personResource = model.createResource(URI_BASE_PERSONS + name.replace(" ", "-"));
            personResource.addLiteral(FOAF.name, name);
            personResource.addLiteral(VCARD.FN, name);
            personResource.addProperty(RDF.type, FOAF.Person);
            personResource.addProperty(RDF.type, FOAF.Agent);
            personResource.addProperty(RDF.type, DCTerms.Agent);
                        
            // associate with GLUES project:
            Resource gluesProject = model.createResource(URI_GLUES_PROJECT);
            gluesProject.addProperty(RDF.type, FOAF.Project);
            gluesProject.addLiteral(FOAF.name, GLUES_PROJECT_NAME_LONG);
            gluesProject.addLiteral(FOAF.name, GLUES_PROJECT_NAME_SHORT);
            gluesProject.addProperty(FOAF.homepage, GLUES_PROJECT_URL);
            gluesProject.addProperty(FOAF.member, personResource);
            personResource.addProperty(FOAF.currentProject, URI_GLUES_PROJECT);
            
            // read out position name:
            addLiteral(personResource, responsibleParty.getPositionName(), VCARD.ROLE);
            
            // create resource for organization:
            if (responsibleParty.getOrganisationName() != null) {
                String orgName = responsibleParty.getOrganisationName().getCharacterString();
                Resource orgResource = model.createResource(URI_BASE_ORGANIZATIONS + orgName.replace(" ", "-"));
                orgResource.addLiteral(FOAF.name, orgName);
                orgResource.addProperty(RDF.type, FOAF.Organization);
                
                orgResource.addProperty(FOAF.member, personResource);
            }
            
            // read out phone numbers:
            if (responsibleParty.getContactInfo() != null) {
                CIContactType contactInfo = responsibleParty.getContactInfo().getCIContact();
                
                if (contactInfo.getPhone() != null)  {
                    if (contactInfo.getPhone().getCITelephone() != null) {
                        addLiterals(personResource, contactInfo.getPhone().getCITelephone().getVoiceArray(), VCARD.TEL);
                    }
                }
            
                // read out email address:
                if(contactInfo.getAddress() != null) {
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
                    ownerResource.addProperty(DC.publisher, personResource);
                }
                else if (contactRoleCode != null && contactRoleCode.equals("distributor")) {
                    ownerResource.addProperty(DC.publisher, personResource);
                }
                else if (contactRoleCode != null && contactRoleCode.equals("pointOfContact")) {
                    ownerResource.addProperty(DC.creator, personResource);
                }
                else if (contactRoleCode != null && contactRoleCode.equals("processor")) {
                    // this 'if' means we are dealing with a provenance processor ... 
                    ownerResource.addProperty(PROV.influencer, personResource);
                    // ... so add further properties:
                    personResource.addProperty(RDF.type, PROV.Person);
                    ownerResource.addProperty(PROV.wasAssociatedWith, personResource);
                }
                else {
                    throw new OXFException("Contact role code '" + contactRoleCode + "' not supported.");
                }
            }
            
            return personResource;
        }
        else {
            throw new OXFException("Non-individual contacts not yet supported.");
        }
    }

    /**
     * Adds a String value as a literal to a resource after checking whether the characterStringProperty is null.
     * 
     * @param resource the {@link Resource} to which a property is added as a literal
     * @param characterStringProperty the {@link String} value of this {@link CharacterStringPropertyType} is added to the resource
     * @param property the {@link Property} which is added to the resource
     * @return true if the literal is added; false otherwise.
     */
    private static boolean addLiteral(Resource resource,
            CharacterStringPropertyType characterStringProperty,
            Property property)
    {
        if (characterStringProperty != null) {
            resource.addLiteral(property, characterStringProperty.getCharacterString());
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Adds all String values of a characterStringPropertyArray as literals to the resource.
     * 
     * @param resource the {@link Resource} to which the properties are added as literals
     * @param characterStringPropertyArray the {@link String} values of this {@link CharacterStringPropertyType[]} are added to the resource
     * @param property the {@link Property} which is added to the resource
     * @return true if any literal is added; false otherwise.
     */
    private static boolean addLiterals(Resource resource,
            CharacterStringPropertyType[] characterStringPropertyArray,
            Property property)
    {
        if (characterStringPropertyArray != null) {
            for (int i = 0; i < characterStringPropertyArray.length; i++) {
                resource.addLiteral(property, characterStringPropertyArray[i].getCharacterString());
            }
            return true;
        }
        else {
            return false;
        }
    }
}
