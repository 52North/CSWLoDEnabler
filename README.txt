
To make this work the following non-maven libs need to be downloaded:

http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDownload/virt_jena.jar
http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDownload/virtjdbc3.jar
http://virtuoso.openlinksw.com/dataspace/dav/wiki/Main/VOSDownload/virtjdbc4.jar


Then run something like the following from the directory where you put the above files (see also: http://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html):

mvn install:install-file -Dfile=virt_jena.jar -DgroupId=com.virtuoso -DartifactId=virt_jena -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=virtjdbc3.jar -DgroupId=com.virtuoso -DartifactId=virtjdbc3 -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=virtjdbc4.jar -DgroupId=com.virtuoso -DartifactId=virtjdbc4 -Dversion=1.0.0 -Dpackaging=jar