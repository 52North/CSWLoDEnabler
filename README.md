# CSW to LOD Translator

This project maps the entries of an OGC Catalog (CSW, http://www.opengeospatial.org/standards/cat) to Linked Open Data using Jena (https://jena.apache.org/) and inserts the results into a Virtuoso Triple Store (http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/). This software is developed in the project [GLUES](https://wiki.52north.org/bin/view/Projects/GLUES).

# Features

* Supports CSW version x.x

# Build Requirements

The project uses Maven (http://maven.apache.org/).

To make this work the following non-Maven libs need to be downloaded and installed to the local repository manually:

* Download the following files into a local directory (for details see http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSDownload)
  * http://opldownload.s3.amazonaws.com/uda/virtuoso/rdfproviders/jena/210/virt_jena2.jar
  * http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDownload/virtjdbc4.jar
* Open a command line in the directory where the files are stored
* Run the following commands (see also: http://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html). You should see a ``BUILD SUCCESS`` message for both commands
  * mvn install:install-file "-Dfile=virt_jena2.jar" "-DgroupId=com.virtuoso" "-DartifactId=virt_jena2" "-Dversion=1.0.0" "-Dpackaging=jar"
  * mvn install:install-file "-Dfile=virtjdbc4.jar" "-DgroupId=com.virtuoso" "-DartifactId=virtjdbc4" "-Dversion=1.0.0" "-Dpackaging=jar"

# Build, Configure, and Run

The project is best run by importing it into an Eclipse workspace. Command-line execution of the main class ``CSWLoDEnabler`` is certainly possible but not tested or documented yet.

## Create configuration file

Go to ``src/main/resources`` and create a copy of the file ``lod.properties.original`` under the name ``lod.properties``. Open it and adjust the Virtuoso user and password.

## Virtuoso

* Install Virtuoso Open Source Edition > http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/
* Configure the Virtuoso DBMS endpoint, e.g. ``URL_VIRTUOSO_JDBC jdbc:virtuoso://localhost:1111`` 

## Pubby

This is a [pubby](http://wifo5-03.informatik.uni-mannheim.de/pubby/) configuration file for using pubby with a Virtuoso running on the default port on localhost for the GLUES dataset, i.e. pubby is deployed at ``http://localhost:8080/glues-lod/``.

```
# Pubby: http://wifo5-03.informatik.uni-mannheim.de/pubby/
# Example config file: http://wifo5-03.informatik.uni-mannheim.de/pubby/config.ttl

# Prefix declarations to be used in RDF output
@prefix conf: <http://richard.cyganiak.de/2007/pubby/config.rdf#> .
@prefix meta: <http://example.org/metadata#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix dc: <http://purl.org/dc/elements/1.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> .
@prefix dbpedia: <http://localhost:8080/resource/> .
@prefix p: <http://localhost:8080/property/> .
@prefix yago: <http://localhost:8080/class/yago/> .
@prefix units: <http://dbpedia.org/units/> .
@prefix geonames: <http://www.geonames.org/ontology#> .
@prefix prv:      <http://purl.org/net/provenance/ns#> .
@prefix prvTypes: <http://purl.org/net/provenance/types#> .
@prefix doap:     <http://usefulinc.com/ns/doap#> .
@prefix void:     <http://rdfs.org/ns/void#> .
@prefix ir:       <http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl#> .
@prefix glues:	<http://glues.52north.org/> .
@prefix prov:	<http://www.w3.org/ns/prov#> .
@prefix vcard:	<http://www.w3.org/2001/vcard-rdf/3.0#> .

<> a conf:Configuration;
    conf:projectName "GLUES LOD";
    conf:projectHomepage <http://nachhaltiges-landmanagement.de/en/scientific-coordination-glues/>;
	# conf:webBase <http://glues.52north.org/>;
    conf:webBase <http://localhost:8080/glues-lod/>;
    conf:usePrefixesFrom <>;
    conf:defaultLanguage "en";
    conf:indexResource <http://glues.52north.org/resource/project/GLUES>;
	conf:labelProperty rdfs:label, dc:title, foaf:name;
	conf:commentProperty rdfs:comment, dc:description;
	conf:imageProperty foaf:depiction;
	
    conf:dataset [
		# Deployment configuration
        #conf:sparqlEndpoint <http://glues.52north.org:8890/sparql>;
        #conf:sparqlDefaultGraph <http://glues.52north.org/>;
        #conf:datasetBase <http://glues.52north.org>;
        #conf:webResourcePrefix "";
		#conf:addSameAsStatements "true";

		# Development configuration
		conf:sparqlEndpoint <http://localhost:8890/sparql>;
        conf:sparqlDefaultGraph <http://glues.52north.org/>;
        conf:datasetBase <http://glues.52north.org>;
        conf:webResourcePrefix "";
		conf:addSameAsStatements "true";
		
        conf:metadataTemplate "metadata.ttl";
    ];
    .
```

## Finding issues

To discover issues with data that is retrieved from a catalog, a specific log file ``/logs/csw2lod-issues.log`` exists that contains all warnings and errors that occured during an execution and is emptied each day. More detailed logging is in the other logging files in ``/logs``.

# Development

Some development notes:

* We use GitHub issues for task management and a Waffle.io board: https://waffle.io/52North/CSWLoDEnabler
* Please use the 52°North code formatter in Eclipse: https://wiki.52north.org/bin/view/Documentation/WebHome#Software_Development

# Contact / Support

Contact: Daniel Nüst (d.nuest@52north.org)

For support questions, please contact the 52°North Metadata Community mailing list: http://52north.org/resources/mailing-list-and-forums/

# License

..