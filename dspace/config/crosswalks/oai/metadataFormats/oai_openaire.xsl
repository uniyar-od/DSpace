<?xml version="1.0" encoding="UTF-8" ?>
<!-- The contents of this file are subject to the license and copyright detailed 
	in the LICENSE and NOTICE files at the root of the source tree and available 
	online at http://www.dspace.org/license/ Developed by DSpace @ Lyncode <dspace@lyncode.com> 
	> http://www.openarchives.org/OAI/2.0/oai_dc.xsd xmlns:oai_oa="https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd" -->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:doc="http://www.lyncode.com/xoai" version="1.0"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:datacite="http://datacite.org/schema/kernel-4"
	xmlns:oaire="http://namespace.openaire.eu/schema/oaire/">
	<xsl:output omit-xml-declaration="yes" method="xml"
		indent="yes" />

	<xsl:template match="/">
		<oaire:resource
			xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://namespace.openaire.eu/schema/oaire/ https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd">

			<!-- DC TITLE -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
				<datacite:title>
					<xsl:value-of select="." />
				</datacite:title>
			</xsl:for-each>
			<!-- DC CONTRIBUTOR AUTHOR -->
			<datacite:creators>
				<xsl:for-each
					select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:field[@name='value']">
					<datacite:creator>
						<datacite:creatorName>
							<xsl:value-of select="." />
						</datacite:creatorName>
					</datacite:creator>
				</xsl:for-each>
			</datacite:creators>

			<!-- DATES -->
			<datacite:dates>
				<xsl:for-each
					select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name!='issued']/doc:element/doc:field[@name='value']">
					<datacite:date>
						<xsl:attribute name="dateType">Issued</xsl:attribute>
						<xsl:value-of select="." />
					</datacite:date>
				</xsl:for-each>

				<!-- StartDate and EndDate attribute will be provided if DRM access is 
					setting to EMBARGOED -->
				<!-- Choose from the date type vocabulary the controlled term ACCEPTED 
					to indicate the start and the term AVAILABLE to indicate the end of an embargo 
					period.--> 
					<!-- DRM structure for embargoed access "embargoed access|||ACCEPTED|||AVAILABLE" -->
				<xsl:variable name="drm">
					<xsl:value-of
						select="doc:metadata/doc:element[@name='dc']/doc:element[@name='others']/doc:element[@name='drm']" />
				</xsl:variable>
				<xsl:if test="contains($drm,'embargoed')">
					<datacite:date>
						<xsl:attribute name="dateType">Accepted</xsl:attribute>
						<xsl:value-of select="substring-before(substring-after($drm,'|||'),'|||')" />
					</datacite:date>
					<datacite:date>
						<xsl:attribute name="dateType">Available</xsl:attribute>
						<xsl:value-of select="substring-after(substring-after($drm,'|||'),'|||')" />
					</datacite:date>
				</xsl:if>
			</datacite:dates>

			<!-- HANDLE -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
				<xsl:variable name="isHandle">
					<xsl:call-template name="isHandle">
						<xsl:with-param name="field" select="." />
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isHandle = 'true'">
						<datacite:identifier>
							<xsl:attribute name="identifierType"><xsl:text>Handle</xsl:text></xsl:attribute>
							<xsl:value-of
								select="./doc:element[@name='uri']/doc:element/doc:field[@name='value']" />
						</datacite:identifier>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>

			<xsl:apply-templates
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='others']/doc:element[@name='drm']"
				mode="datacite" />
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name!='author']/doc:element/doc:field[@name='value']">
				<dc:contributor>
					<xsl:value-of select="." />
				</dc:contributor>
			</xsl:for-each>

			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']">
				<oaire:resourceType>
					<xsl:value-of select="." />
				</oaire:resourceType>
			</xsl:for-each>

			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='others']/doc:element/doc:element/doc:field[@name='value']">
				<dc:source>
					<xsl:value-of select="."></xsl:value-of>
				</dc:source>
			</xsl:for-each>
			<!-- CREATIVE COMMON LICENSE -->
			<xsl:apply-templates
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='others']/doc:element[@name='cc']/doc:field[@name='value']"
				mode="oaire" />

			<!-- HANDLE -->
			<datacite:alternateIdentifiers>
				<xsl:apply-templates
					select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']"
					mode="datacite" />
			</datacite:alternateIdentifiers>

			

			<!-- DC PUBLISHER -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:element/doc:field[@name='value']">
				<dc:publisher>
					<xsl:value-of select="." />
				</dc:publisher>
			</xsl:for-each>
			<!-- DC LANGUAGE ISO -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='language']/doc:element/doc:element/doc:field[@name='value']">
				<dc:language>
					<xsl:value-of select="." />
				</dc:language>
			</xsl:for-each>
			<!-- ACCESS RIGHTS USARE DIGITAL RIGHT MANAGEMENT MODIFICARE LA SELECT! -->

			<!-- FOUNDING REFERENCES USARE DIGITAL RIGHT MANAGEMENT MODIFICARE LA 
				SELECT! -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:field[@name='value']">
				<oaire:fundingReference>
					<xsl:value-of select="." />
				</oaire:fundingReference>
			</xsl:for-each>

			<!-- DC DESCRIPTION ABSTRACT -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:field[@name='value']">
				<dc:description>
					<xsl:value-of select="." />
				</dc:description>
			</xsl:for-each>
			
			<!-- DC SUBJECT -->
			<xsl:apply-templates
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']"
				mode="datacite">
			</xsl:apply-templates>
			
			<!-- DC DESCRIPTION -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:field[@name='value']">
				<dc:description>
					<xsl:value-of select="." />
				</dc:description>
			</xsl:for-each>
			
			<!-- FILE LOCATION for each available bitstream including accessRightsURI 
				and mimeType; for the objectType subproperty see the optional activities -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']/doc:element/doc:element[@name='bitstreams']/doc:element[@name='bitstream']/doc:field[@name='format']">
				<oaire:file>
					accessRightsURI="http://purl.org/coar/access_right/c_abf2">
					<xsl:value-of select="." />
				</oaire:file>
			</xsl:for-each>
			
			<!-- DATACITE SIZE -->
			<xsl:apply-templates
				select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']"
				mode="datacite">
			</xsl:apply-templates>
			
			<!-- CITATION TITLE -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']">
				<oaire:citationTitle>
					<xsl:value-of select="." />
				</oaire:citationTitle>
			</xsl:for-each>
			
			<!-- CITATION VOLUME -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartofseries']/doc:element/doc:field[@name='value']">
				<oaire:citationVolume>
					<xsl:value-of select="." />
				</oaire:citationVolume>
			</xsl:for-each>
			<!-- DRM -->



			<!-- CITATION ISSUE -->
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:element/doc:field[@name='value']">
				<oaire:citationIssue>
					<xsl:value-of select="." />
				</oaire:citationIssue>
			</xsl:for-each>
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element/doc:field[@name='value']">
				<dc:source>
					<xsl:value-of select="." />
				</dc:source>
			</xsl:for-each>
			<xsl:for-each
				select="doc:metadata/doc:element[@name='dc']/doc:element[@name='source']/doc:element/doc:element/doc:field[@name='value']">
				<dc:source>
					<xsl:value-of select="." />
				</dc:source>
			</xsl:for-each>

			<xsl:for-each
				select="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']">
				<xsl:if test="doc:field[@name='name']/text() = 'ORIGINAL'">
					<xsl:for-each
						select="doc:element[@name='bitstreams']/doc:element">
						<oaire:file>
							<xsl:attribute name="accessRightsURI">
										<xsl:value-of
								select="doc:field[@name='url']/text()"></xsl:value-of>
									</xsl:attribute>
							<xsl:attribute name="mimeType">
										<xsl:value-of
								select="doc:field[@name='format']/text()"></xsl:value-of>
									</xsl:attribute>
						</oaire:file>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>
		</oaire:resource>
	</xsl:template>

	<!-- datacite:sizes -->
	<!-- https://openaire-guidelines-for-literature-repository-managers.readthedocs.io/en/v4.0.0/field_size.html -->
	<xsl:template
		match="doc:element[@name='bundles']/doc:element[@name='bundle']"
		mode="datacite">
		<xsl:if test="doc:field[@name='name' and text()='ORIGINAL']">
			<datacite:sizes>
				<xsl:for-each
					select="doc:element[@name='bitstreams']/doc:element[@name='bitstream']">
					<xsl:apply-templates
						select="./doc:element[@name='size']" mode="datacite" />
				</xsl:for-each>
			</datacite:sizes>
		</xsl:if>
	</xsl:template>

	<xsl:template match="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']" mode="datacite">
				<xsl:variable name="identifierType">
					<xsl:call-template name="resolveFieldType">
						<xsl:with-param name="field" select="./doc:element/doc:field[@name='value'][1]" />
					</xsl:call-template>
				</xsl:variable>
				<!-- only process the first element -->
				<datacite:identifier>
					<xsl:attribute name="identifierType">
                <xsl:value-of select="$identifierType" />
            </xsl:attribute>
					<xsl:value-of select="./doc:element/doc:field[@name='value'][1]" />
				</datacite:identifier>
			</xsl:template>
			
	<!-- datacite:subjects -->
	<!-- https://openaire-guidelines-for-literature-repository-managers.readthedocs.io/en/v4.0.0/field_subject.html -->
			<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='subject']"
		mode="datacite">
		<datacite:subjects>
			<xsl:for-each select="./doc:element">
				<xsl:apply-templates select="." mode="datacite" />
			</xsl:for-each>
		</datacite:subjects>
	</xsl:template>

	<!-- datacite:subject -->
	<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='subject']/doc:element"
		mode="datacite">
		<xsl:for-each select="./doc:field[@name='value']">
			<datacite:subject>
				<xsl:value-of select="./text()" />
			</datacite:subject>
		</xsl:for-each>
	</xsl:template>
	<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='identifier']"
		mode="datacite_ids">
		<xsl:variable name="alternateIdentifierType">
			<xsl:call-template name="getRelatedIdentifierType">
				<xsl:with-param name="element"
					select="./doc:element[@name!='issn']" />
			</xsl:call-template>
		</xsl:variable>

		<!-- Don't consider Handles as related identifiers -->
		<!-- relationType="Continues" relatedMetadataScheme="" schemeURI="" schemeType="" -->

		<xsl:variable name="isHandle">
			<xsl:call-template name="isHandle">
				<xsl:with-param name="field" select="./doc:field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="$isHandle = 'false'">
			<datacite:alternateIdentifier>
				<xsl:attribute name="relatedIdentifierType">
                    <xsl:value-of
					select="$alternateIdentifierType" />
                </xsl:attribute>
				<xsl:value-of
					select="./doc:element/doc:field[@name='value']/text()" />
			</datacite:alternateIdentifier>
		</xsl:if>
	</xsl:template>

	<!-- ISSN -->
	<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='issn']"
		mode="datacite">
		<datacite:relatedIdentifier>
			<xsl:attribute name="relatedIdentifierType">ISSN</xsl:attribute>
			<xsl:attribute name="relationType">ISSN</xsl:attribute>
			<xsl:value-of select="." />
		</datacite:relatedIdentifier>
	</xsl:template>

	<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name!='issn']"
		mode="datacite">
		<datacite:alternateIdentifier>
			<xsl:for-each select="./doc:element">
				<xsl:apply-templates select="."
					mode="datacite_ids" />
			</xsl:for-each>
		</datacite:alternateIdentifier>
	</xsl:template>

	<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='others']/doc:element[@name='drm']"
		mode="datacite">
		<xsl:variable name="rightsURI">
			<xsl:call-template name="resolveRightsURI">
				<xsl:with-param name="field"
					select="doc:field[@name='value']/text()" />
			</xsl:call-template>
		</xsl:variable>
		<datacite:rights>
			<xsl:if test="$rightsURI">
				<xsl:attribute name="uri">
                <xsl:value-of select="$rightsURI" />
            </xsl:attribute>
			</xsl:if>
			<xsl:value-of
				select="substring-before(doc:field[@name='value']/text(),'|||')" />
		</datacite:rights>
	</xsl:template>

	<!-- License CC splitter -->
	<xsl:template
		match="doc:element[@name='dc']/doc:element[@name='others']/doc:element[@name='cc']/doc:field[@name='value']"
		mode="oaire">
		<oaire:licenseCondition>
			<xsl:attribute name="startDate">
			<xsl:value-of
				select="substring-after(substring-after(.,'|||'),'|||')" />
		</xsl:attribute>
			<xsl:attribute name="uri">
			<xsl:value-of
				select="substring-before(substring-after(.,'|||'),'|||')" />
		</xsl:attribute>
			<xsl:value-of select="substring-before(.,'|||')"></xsl:value-of>
		</oaire:licenseCondition>
	</xsl:template>

	<xsl:param name="uppercase"
		select="'ABCDEFGHIJKLMNOPQRSTUVWXYZÀÈÌÒÙÁÉÍÓÚÝÂÊÎÔÛÃÑÕÄËÏÖÜŸÅÆŒÇÐØ'" />
	
	<xsl:param name="smallcase"
		select="'abcdefghijklmnopqrstuvwxyzàèìòùáéíóúýâêîôûãñõäëïöüÿåæœçðø'" />

	<!-- will try to resolve the field type based on the value -->

	<xsl:template name="resolveFieldType">
		<xsl:param name="field" />
		<!-- regexp not supported on XSLTv1 -->
		<xsl:variable name="isHandle">
			<xsl:call-template name="isHandle">
				<xsl:with-param name="field" select="$field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="isDOI">
			<xsl:call-template name="isDOI">
				<xsl:with-param name="field" select="$field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="isURL">
			<xsl:call-template name="isURL">
				<xsl:with-param name="field" select="$field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$isHandle = 'true'">
				<xsl:text>Handle</xsl:text>
			</xsl:when>
			<xsl:when test="$isDOI = 'true'">
				<xsl:text>DOI</xsl:text>
			</xsl:when>
			<xsl:when test="$isURL = 'true' and $isHandle = 'false'">
				<xsl:text>URL</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>N/A</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- it will verify if a given field is an handle -->
	<xsl:template name="isHandle">
		<xsl:param name="field" />
		<xsl:choose>
			<xsl:when test="$field[contains(text(),'hdl.handle.net')]">
				<xsl:value-of select="true()" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="false()" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- it will verify if a given field is a DOI -->
	<xsl:template name="isDOI">
		<xsl:param name="field" />
		<xsl:choose>
			<xsl:when
				test="$field[contains(text(),'doi.org') or starts-with(text(),'10.')]">
				<xsl:value-of select="true()" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="false()" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- it will verify if a given field is an ORCID -->
	<xsl:template name="isORCID">
		<xsl:param name="field" />
		<xsl:choose>
			<xsl:when test="$field[contains(text(),'orcid.org')]">
				<xsl:value-of select="true()" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="false()" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- it will verify if a given field is an URL -->
	<xsl:template name="isURL">
		<xsl:param name="field" />
		<xsl:variable name="lc_field">
			<xsl:call-template name="lowercase">
				<xsl:with-param name="value" select="$field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when
				test="$lc_field[starts-with(text(),'http://') or starts-with(text(),'https://')]">
				<xsl:value-of select="true()" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="false()" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- to retrieve a string in lowercase -->
	<xsl:template name="lowercase">
		<xsl:param name="value" />
		<xsl:value-of
			select="translate($value, $uppercase, $smallcase)" />
	</xsl:template>

	<!-- This template will retrieve the identifier type based on the element 
		name -->
	<!-- there are some special cases like DOI or HANDLE which the type is also 
		inferred from the value itself -->
	<xsl:template name="getRelatedIdentifierType">
		<xsl:param name="element" />
		<xsl:variable name="lc_identifier_type">
			<xsl:call-template name="lowercase">
				<xsl:with-param name="value" select="$element/@name" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="isHandle">
			<xsl:call-template name="isHandle">
				<xsl:with-param name="field"
					select="$element/doc:field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="isDOI">
			<xsl:call-template name="isDOI">
				<xsl:with-param name="field"
					select="$element/doc:field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="isURL">
			<xsl:call-template name="isURL">
				<xsl:with-param name="field"
					select="$element/doc:field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$lc_identifier_type = 'ark'">
				<xsl:text>ARK</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'arxiv'">
				<xsl:text>arXiv</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'bibcode'">
				<xsl:text>bibcode</xsl:text>
			</xsl:when>
			<xsl:when
				test="$isDOI = 'true' or $lc_identifier_type = 'doi'">
				<xsl:text>DOI</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'ean13'">
				<xsl:text>EAN13</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'eissn'">
				<xsl:text>EISSN</xsl:text>
			</xsl:when>
			<xsl:when
				test="$isHandle = 'true' or $lc_identifier_type = 'handle'">
				<xsl:text>Handle</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'igsn'">
				<xsl:text>IGSN</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'isbn'">
				<xsl:text>ISBN</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'istc'">
				<xsl:text>ISTC</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'lissn'">
				<xsl:text>LISSN</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'lsid'">
				<xsl:text>LSID</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'pmid'">
				<xsl:text>PMID</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'purl'">
				<xsl:text>PURL</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_identifier_type = 'upc'">
				<xsl:text>UPC</xsl:text>
			</xsl:when>
			<xsl:when
				test="$isURL = 'true' or $lc_identifier_type = 'url'">
				<xsl:text>URL</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>URN</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="resolveRightsURI">
		<xsl:param name="field" />
		<xsl:variable name="lc_value">
			<xsl:call-template name="lowercase">
				<xsl:with-param name="value" select="$field" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$lc_value = 'open access'">
				<xsl:text>http://purl.org/coar/access_right/c_abf2</xsl:text>
			</xsl:when>
			<xsl:when
				test="substring-before($lc_value,'|||')= 'embargoed access'">
				<xsl:text>http://purl.org/coar/access_right/c_f1cf</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_value = 'restricted access'">
				<xsl:text>http://purl.org/coar/access_right/c_16ec</xsl:text>
			</xsl:when>
			<xsl:when test="$lc_value = 'metadata only access'">
				<xsl:text>http://purl.org/coar/access_right/c_14cb</xsl:text>
			</xsl:when>
			<xsl:otherwise />
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>