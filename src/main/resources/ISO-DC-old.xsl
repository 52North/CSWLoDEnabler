<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
	xmlns:dc="http://purl.org/dc/elements/1.1" 
	xmlns:dcterms="http://purl.org/dc/terms" 
	xmlns:iso19115="http://www.isotc211.org/iso19115/" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	
	<xsl:output method="xml" indent="yes" encoding="ISO-8859-1" />
	<xsl:output doctype-system="DCMES.dtd" />
	<!-- this line will change if the location of the DTD changes. -->
	<!-- Stylesheet that translates a correct ISO data into a correct DC data -->
	<!-- Author: IAAA -->
	<xsl:template match="/">
		<xsl:apply-templates select="iso19115:MD_Metadata" />
	</xsl:template>
	<xsl:template match="iso19115:MD_Metadata">
		<xsl:variable name="xsltsl-str-lower" select="'a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p;q;r;s;t;u;v;w;x;y;z'" />
		<xsl:variable name="xsltsl-str-upper" select="'A;B;C;D;E;F;G;H;I;J;K;L;M;N;O;P;Q;R;S;T;U;V;W;X;Y;Z'" />
		<xsl:element name="rdf:RDF">
			<xsl:element name="rdf:Description">
				<!-- Element title conversion: -->
				<!-- It's supposed to be more than one ocurrence in ISO (and that -->
				<!-- means more than citation element). For each ocurrence of -->
				<!-- citation.title a new element dc:title will be generated -->
				<!-- ISO CORE and ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/citation/title">
					<xsl:element name="dc:title">
						<xsl:value-of select="normalize-space(.)" />
					</xsl:element>
				</xsl:for-each>
				
				<!-- Also a new element dc:title is generated for each -->
				<!--citation.alternateTitle element -->
				<xsl:for-each select="./iso19115:_MD_Identification/citation/alternateTitle">
					<xsl:element name="dc:title">
						<xsl:value-of select="normalize-space(.)" />
					</xsl:element>
				</xsl:for-each>
				<!-- creator element conversion: -->
				<!-- It's assumed that at least, one of organisationName, individualName -->
				<!-- or positionName exists. No comprobations are made. -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/citation/citedResponsibleParty">
					<xsl:if test="normalize-space(./role/CI_RoleCode_CodeList)='originator'">
						<xsl:element name="dc:creator">
							<xsl:choose>
								<xsl:when test="./organisationName">
									<xsl:value-of select="./organisationName" />
								</xsl:when>
								<xsl:when test="./individualName">
									<xsl:value-of select="./individualName" />
								</xsl:when>
								<xsl:when test="./positionName">
									<xsl:value-of select="./positionName" />
								</xsl:when>
								<xsl:otherwise>
									<!-- Nothing. This should be never happen. -->
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- ISO CORE -->
				<xsl:for-each select="./iso19115:_MD_Identification/pointOfContact">
					<xsl:if test="normalize-space(./role/CI_RoleCode_CodeList)='originator'">
						<xsl:element name="dc:creator">
							<xsl:choose>
								<xsl:when test="./organisationName">
									<xsl:value-of select="./organisationName" />
								</xsl:when>
								<xsl:when test="./individualName">
									<xsl:value-of select="./individualName" />
								</xsl:when>
								<xsl:when test="./positionName">
									<xsl:value-of select="./positionName" />
								</xsl:when>
								<xsl:otherwise>
									<!-- Nothing. This should be never happen. -->
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- Element subject conversion -->
				<!-- The data is supposed to be correct and for this reason the -->
				<!-- verification of the correction of the value is not made. -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/descriptiveKeywords">
					<xsl:if test="normalize-space(./type/MD_KeywordTypeCode_CodeList)='theme'">
						<xsl:element name="dc:subject">
							<xsl:value-of select="./keyword" />
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- ISO CORE -->
				<xsl:for-each select="./iso19115:_MD_Identification/topicCategory">
					<xsl:element name="dc:subject">
						<xsl:value-of select="normalize-space(./MD_TopicCategoryCode_CodeList)" />
					</xsl:element>
				</xsl:for-each>
				<!-- description element conversion -->
				<!-- For each ocurrence of abstract a new dc:description will be -->
				<!-- generated -->
				<!-- ISO CORE E ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/abstract">
					<xsl:element name="dc:description">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
				<!-- publisher element conversion: -->
				<!-- It's assumed that at least, one of organisationName, individualName -->
				<!-- or positionName exists. No comprobations are made. -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/citation/citedResponsibleParty">
					<xsl:if test="normalize-space(./role/CI_RoleCode_CodeList)='publisher'">
						<xsl:element name="dc:publisher">
							<xsl:choose>
								<xsl:when test="./organisationName">
									<xsl:value-of select="./organisationName" />
								</xsl:when>
								<xsl:when test="./individualName">
									<xsl:value-of select="./individualName" />
								</xsl:when>
								<xsl:when test="./positionName">
									<xsl:value-of select="./positionName" />
								</xsl:when>
								<xsl:otherwise>
									<!-- Nothing. This should be never happen. -->
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/citation/pointOfContact">
					<xsl:if test="normalize-space(./role/CI_RoleCode_CodeList)='publisher'">
						<xsl:element name="dc:publisher">
							<xsl:choose>
								<xsl:when test="./organisationName">
									<xsl:value-of select="./organisationName" />
								</xsl:when>
								<xsl:when test="./individualName">
									<xsl:value-of select="./individualName" />
								</xsl:when>
								<xsl:when test="./positionName">
									<xsl:value-of select="./positionName" />
								</xsl:when>
								<xsl:otherwise>
									<!-- Nothing. This should be never happen. -->
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- ISO CORE -->
				<xsl:for-each select="./contact">
					<xsl:if test="normalize-space(./role/CI_RoleCode_CodeList)='publisher'">
						<xsl:element name="dc:publisher">
							<xsl:choose>
								<xsl:when test="./organisationName">
									<xsl:value-of select="./organisationName" />
								</xsl:when>
								<xsl:when test="./individualName">
									<xsl:value-of select="./individualName" />
								</xsl:when>
								<xsl:when test="./positionName">
									<xsl:value-of select="./positionName" />
								</xsl:when>
								<xsl:otherwise>
									<!-- Nothing. This should be never happen. -->
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- contributor element conversion -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/credit">
					<xsl:element name="dc:contributor">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
				<!-- ISO CORE N/A ¡¡¡GAP!!! -->
				<!-- date element conversion: In ISO the publication date is compulsory -->
				<!-- and it can appear N times -->
				<!-- ISO CORE E ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/citation/date">
					<xsl:if test="normalize-space(./dateType/CI_DateTypeCode_CodeList)='publication'">
						<xsl:element name="dc:date">
							<xsl:value-of select="./date" />
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- Another publication date will be generated with the value of -->
				<!-- dateStamp. -->
				<xsl:if test="./dateStamp">
					<xsl:element name="dc:date">
						<xsl:value-of select="./dateStamp" />
					</xsl:element>
				</xsl:if>
				<!-- element type conversion(1) -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./hierarchyLevel">
					<xsl:variable name="hierarchyLevelVar">
						<!-- it's translated to lower case -->
						<xsl:value-of select="translate(normalize-space(./MD_ScopeCode_CodeList),$xsltsl-str-upper,$xsltsl-str-lower)" />
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$hierarchyLevelVar='attribute'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='attributetype'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='collectionhardware'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='collectionsession'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Event</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='dataset'">
							<!-- If upper and lower cases are the same, only one dataset must -->
							<!-- be generated -->
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<!--xsl:element name="dc:type"> <xsl:text>Dataset</xsl:text> </xsl:element -->
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='series'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Collection</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='nongeographicdataset'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='dimensiongroup'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='feature'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='featuretype'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='propertytype'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='fieldsession'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Event</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='software'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Software</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='service'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Service</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='model'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:when test="$hierarchyLevelVar='tile'">
							<xsl:element name="dc:type">
								<xsl:value-of select="normalize-space(./MD_ScopeCode_CodeList)" />
							</xsl:element>
							<xsl:element name="dc:type">
								<xsl:text>Dataset</xsl:text>
							</xsl:element>
						</xsl:when>
						<xsl:otherwise>
							<!-- Nothing. This should be never happen. -->
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
				<!-- element type conversion(2) -->
				<!-- ISO CORE -->
				<xsl:for-each select="./iso19115:_MD_Identification/spatialRepresentationType">
					<xsl:element name="dc:type">
						<xsl:value-of select="normalize-space(./MD_SpatialRepresentationTypeCode_CodeList)" />
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/citation/presentationForm">
					<xsl:element name="dc:type">
						<xsl:value-of select="normalize-space(./CI_PresentationFormCode_CodeList)" />
					</xsl:element>
				</xsl:for-each>
				<!-- Format element conversion -->
				<!-- ISO COMPREHENSIVE E ISO CORE -->
				<xsl:for-each select="./distributionInfo/distributionFormat">
					<xsl:element name="dc:format">
						<xsl:value-of select="./name" />
						<xsl:text>//</xsl:text>
						<xsl:value-of select="./version" />
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/resourceFormat">
					<xsl:element name="dc:format">
						<xsl:value-of select="./name" />
						<xsl:text>//</xsl:text>
						<xsl:value-of select="./version" />
					</xsl:element>
				</xsl:for-each>
				<!-- identifier element conversion: -->
				<!-- For this element there are five possible ways. -->
				<xsl:for-each select="./distributionInfo/transferOptions/onLine">
					<xsl:element name="dc:identifier">
						<xsl:if test="./linkage">
							<xsl:value-of select="./linkage" />
						</xsl:if>
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/citation/identifier">
					<xsl:element name="dc:identifier">
						<xsl:value-of select="./code" />
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/citation/ISBN">
					<xsl:variable name="isbnVar">
						<!-- it's translated to lower case -->
						<xsl:value-of select="translate(normalize-space(.),$xsltsl-str-upper, $xsltsl-str-lower)" />
					</xsl:variable>
					<xsl:element name="dc:identifier">
						<xsl:choose>
							<xsl:when test="contains($isbnVar, 'isbn')">
								<xsl:value-of select="." />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>ISBN </xsl:text>
								<xsl:value-of select="." />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/citation/ISSN">
					<xsl:variable name="issnVar">
						<!-- it's translated to lower case -->
						<xsl:value-of select="translate(normalize-space(.),$xsltsl-str-upper, $xsltsl-str-lower)" />
					</xsl:variable>
					<xsl:element name="dc:identifier">
						<xsl:choose>
							<xsl:when test="contains($issnVar, 'ISSN')">
								<xsl:value-of select="." />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>ISSN </xsl:text>
								<xsl:value-of select="." />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:element>
				</xsl:for-each>
				<xsl:if test="./dataSetURI">
					<xsl:element name="dc:identifier">
						<xsl:value-of select="./dataSetURI" />
					</xsl:element>
				</xsl:if>
				<!-- source element conversion: -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./dataQualityInfo/lineage/source/description">
					<xsl:element name="dc:source">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
				<!-- ISO CORE N/A ¡¡¡GAP!!! -->
				<!-- language element conversion. -->
				<!-- ISO COMPREHENSIVE E ISO CORE -->
				<xsl:for-each select="./iso19115:_MD_Identification/language">
					<xsl:if test="./isoCode">
						<xsl:element name="dc:language">
							<xsl:value-of select="./isoCode" />
						</xsl:element>
					</xsl:if>
					<xsl:if test="./isoName">
						<xsl:element name="dc:language">
							<xsl:value-of select="./isoName" />
						</xsl:element>
					</xsl:if>
					<xsl:if test="./otherLang">
						<xsl:element name="dc:language">
							<xsl:value-of select="./otherLang" />
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
				<!-- relation element conversion: -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:for-each select="./iso19115:_MD_Identification/citation/series/name">
					<xsl:element name="dc:relation">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="./iso19115:_MD_Identification/aggregationInfo/aggregateDataSetIdentifier/code">
					<xsl:element name="dc:relation">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
				<!-- ISO CORE N/A ¡¡¡GAP!!! -->
				<!-- coverage element conversion: -->
				<!-- It's composed by several elements. -->
				<!-- PlaceName: keywords that have type = "place". -->
				<!-- Coordenates: iso19115:EX_GeographicBoundingBox. -->
				<!-- PeriodName: keywords that have type = "period". -->
				<!-- Period of time: temporalElement. -->
				<!-- If in the data ISO there is no information, the element won't be -->
				<!-- generated, but, if at least one of the four elements exists, the -->
				<!-- element will be generated. -->
				<xsl:choose>
					<xsl:when test="count(./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='place']) > 0 or ./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/northBoundLatitude or count(./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='temporal']) > 0 or ./iso19115:_MD_Identification/extent/temporalElement/extent/beginEnd"> 
						<xsl:element name="dc:coverage">
							<!-- element dcterms:Box -->
							<xsl:choose>
								<xsl:when test="count(./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='place']) > 0 or ./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/northBoundLatitude">
									<xsl:element name="dcterms:Box">
										<xsl:element name="Box">
											<!-- ISO COMPREHENSIVE -->
											<!-- If there are two or more keywords of type place, there are concatenated with a comma -->
											<xsl:if test="./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='place']">
												<xsl:attribute name="name">
												<xsl:for-each select="./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='place']">
												<xsl:value-of select="./../keyword" />
												<xsl:if test="position()!=last()">
												<xsl:text disable-output-escaping="yes">&#44;</xsl:text>
												</xsl:if>
												</xsl:for-each>
												</xsl:attribute>
											</xsl:if>
											<!-- ISO CORE N/A ¡¡¡GAP!!! -->
											<!-- ISO COMPREHENSIVE and ISO CORE -->
											<xsl:if test="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/northBoundLatitude">
												<xsl:element name="northlimit">
													<xsl:value-of select="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/northBoundLatitude" />
												</xsl:element>
											</xsl:if>
											<xsl:if test="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/eastBoundLongitude">
												<xsl:element name="eastlimit">
													<xsl:value-of select="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/eastBoundLongitude" />
												</xsl:element>
											</xsl:if>
											<xsl:if test="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/southBoundLatitude">
												<xsl:element name="southlimit">
													<xsl:value-of select="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/southBoundLatitude" />
												</xsl:element>
											</xsl:if>
											<xsl:if test="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/westBoundLongitude">
												<xsl:element name="westlimit">
													<xsl:value-of select="./iso19115:_MD_Identification/extent/iso19115:EX_GeographicBoundingBox/westBoundLongitude" />
												</xsl:element>
											</xsl:if>
										</xsl:element>
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
								</xsl:otherwise>
							</xsl:choose>
							<!-- element dcterms:Period -->
							<xsl:choose>
								<xsl:when test="count(./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='temporal']) > 0 or ./iso19115:_MD_Identification/extent/temporalElement/extent/beginEnd">
									<xsl:element name="dcterms:Period">
										<xsl:element name="Period">
											<!-- ISO COMPREHENSIVE -->
											<xsl:if test="./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='temporal']">
												<xsl:attribute name="name">
												<xsl:for-each select="./iso19115:_MD_Identification/descriptiveKeywords/type[MD_KeywordTypeCode_CodeList='temporal']">
												<xsl:value-of select="./../keyword" />
												<xsl:if test="position()!=last()">
												<xsl:text disable-output-escaping="yes">&#44;</xsl:text>
												</xsl:if>
												</xsl:for-each>
												</xsl:attribute>
											</xsl:if>
											<!-- ISO CORE N/A ¡¡¡GAP!!! -->
											<!-- ISO COMPREHENSIVE and ISO CORE -->
											<xsl:if test="./iso19115:_MD_Identification/extent/temporalElement/extent/beginEnd">
												<xsl:element name="start">
													<xsl:value-of select="./iso19115:_MD_Identification/extent/temporalElement/extent/beginEnd/begin" />
												</xsl:element>
												<xsl:element name="end">
													<xsl:value-of select="./iso19115:_MD_Identification/extent/temporalElement/extent/beginEnd/end" />
												</xsl:element>
											</xsl:if>
										</xsl:element>
									</xsl:element>
								</xsl:when>
								<xsl:otherwise>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:when>
					<xsl:otherwise>
						<!-- There is no information for coverage element -->
					</xsl:otherwise>
				</xsl:choose>
				<!-- rights element conversion. -->
				<!-- ISO COMPREHENSIVE -->
				<xsl:if test="./iso19115:_MD_Identification/resourceConstraints/accessConstraints">
					<xsl:for-each select="./iso19115:_MD_Identification/resourceConstraints/accessConstraints">
						<xsl:variable name="rightsVar">
							<!-- it's translated to lower case -->
							<xsl:value-of select="translate(normalize-space(.),$xsltsl-str-upper,$xsltsl-str-lower)" />
						</xsl:variable>
						<xsl:element name="dc:rights">
							<xsl:choose>
								<xsl:when test="$rightsVar='copyright'">
									<xsl:text>copyright</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='patent'">
									<xsl:text>patent</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='patentpending'">
									<xsl:text>patentPending</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='trademark'">
									<xsl:text>trademark</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='license'">
									<xsl:text>license</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='intellectualpropertyrights'">
									<xsl:text>intellectualPropertyRights</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='restricted'">
									<xsl:text>restricted</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar='otherrestrictions'">
									<xsl:text>otherRestrictions</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<!-- If the value is not in this list the data in ISO is not correct. However, the value is translated to DC. -->
									<xsl:value-of select="normalize-space(.)" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="./iso19115:_MD_Identification/resourceConstraints/useConstraints">
					<!-- There is, at least, one -->
					<xsl:for-each select="./iso19115:_MD_Identification/resourceConstraints/useConstraints">
						<xsl:variable name="rightsVar2">
							<!-- it's translated to lower case -->
							<xsl:value-of select="translate(normalize-space(.),$xsltsl-str-upper,$xsltsl-str-lower)" />
						</xsl:variable>
						<xsl:element name="dc:rights">
							<xsl:choose>
								<xsl:when test="$rightsVar2='copyright'">
									<xsl:text>copyright</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='patent'">
									<xsl:text>patent</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='patentpending'">
									<xsl:text>patentPending</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='trademark'">
									<xsl:text>trademark</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='license'">
									<xsl:text>license</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='intellectualpropertyrights'">
									<xsl:text>intellectualPropertyRights</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='restricted'">
									<xsl:text>restricted</xsl:text>
								</xsl:when>
								<xsl:when test="$rightsVar2='otherrestrictions'">
									<xsl:text>otherRestrictions</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<!-- If the value is not in this list the data in ISO is not correct. However, the value is translated to DC. -->
									<xsl:value-of select="normalize-space(.)" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:element>
					</xsl:for-each>
				</xsl:if>
				<xsl:for-each select="./iso19115:_MD_Identification/resourceConstraints/otherConstraints">
					<xsl:element name="dc:rights">
						<xsl:value-of select="normalize-space(.)" />
					</xsl:element>
				</xsl:for-each>
				<!-- ISO CORE N/A ¡¡¡GAP!!! -->
			</xsl:element>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>