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

This is a [pubby](http://wifo5-03.informatik.uni-mannheim.de/pubby/) configuration file for using pubby with a Virtuoso running on the default port on localhost for the GLUES dataset.

```
..
```

# Development

Some development notes:

* We use GitHub issues for task management and a Waffle.io board: https://waffle.io/52North/CSWLoDEnabler
* Please use the 52°North code formatter in Eclipse: https://wiki.52north.org/bin/view/Documentation/WebHome#Software_Development

# Contact / Support

Contact: Daniel Nüst (d.nuest@52north.org)

For support questions, please contact the 52°North Metadata Community mailing list: http://52north.org/resources/mailing-list-and-forums/

# License

..