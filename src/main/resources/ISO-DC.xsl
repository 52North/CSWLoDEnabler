<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
	xmlns:dc="http://purl.org/dc/elements/1.1" 
	xmlns:dcterms="http://purl.org/dc/terms" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd"  
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<!-- xmlns:iso19115="http://www.isotc211.org/iso19115/" -->
	
	<xsl:output method="xml" indent="yes" />
	
	<xsl:template match="/">
		<xsl:apply-templates select="gmd:MD_Metadata" />
	</xsl:template>
	<xsl:template match="gmd:MD_Metadata">
		<xsl:variable name="xsltsl-str-lower" select="'a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p;q;r;s;t;u;v;w;x;y;z'" />
		<xsl:variable name="xsltsl-str-upper" select="'A;B;C;D;E;F;G;H;I;J;K;L;M;N;O;P;Q;R;S;T;U;V;W;X;Y;Z'" />
		<xsl:element name="rdf:RDF">
			<xsl:element name="rdf:Description">
				<!-- Element title conversion: -->
				<!-- It's supposed to be more than one ocurrence in ISO (and that -->
				<!-- means more than citation element). For each ocurrence of -->
				<!-- citation.title a new element dc:title will be generated -->
				<!-- ISO CORE and ISO COMPREHENSIVE -->
				<xsl:for-each select="//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
					<xsl:element name="dc:title">
						<xsl:value-of select="normalize-space(.)" />
					</xsl:element>
				</xsl:for-each>
				
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>