<?xml version="1.0" encoding="UTF-8"?>
<!-- 

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

	Developed by DSpace @ Lyncode <dspace@lyncode.com> 
	Following OpenAIRE Guidelines 1.1:
		- http://www.openaire.eu/component/content/article/207

 -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:doc="http://www.lyncode.com/xoai">
	<xsl:output indent="yes" method="xml" omit-xml-declaration="yes" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
 	<!-- Formatting dc.date.issued -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field/text()">
		<xsl:call-template name="formatdate">
			<xsl:with-param name="datestr" select="." />
		</xsl:call-template>
	</xsl:template>
	
	<!-- Removing other dc.date.* -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name!='issued']" />
	
	<!-- Prefixing and Modifying dc.rights -->
	<!-- Removing unwanted -->
	<xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:element" />
	<!-- Replacing -->
	<xsl:template match="/doc:metadata/doc:element[@name='item']/doc:element[@name='grantfulltext']/doc:element/doc:field/text()">
		<xsl:choose>
			<xsl:when test="contains(., 'open')">
				<xsl:text>http://purl.org/coar/access_right/c_abf2</xsl:text>
			</xsl:when>
			<xsl:when test="contains(., 'restricted')">
				<xsl:text>http://purl.org/coar/access_right/c_16ec</xsl:text>
			</xsl:when>
			<xsl:when test="contains(., 'embargo')">

					<xsl:text>http://purl.org/coar/access_right/c_f1cf/</xsl:text>
					<xsl:value-of select="substring(., 9, 4)" />
					<xsl:text>-</xsl:text>
					<xsl:value-of select="substring(., 13, 2)" />
					<xsl:text>-</xsl:text>
					<xsl:value-of select="substring(., 15, 2)" />

			</xsl:when>
			<xsl:when test="contains(., 'reserved')">
				<xsl:text>http://purl.org/coar/access_right/c_16ec</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>http://purl.org/coar/access_right/c_14cb</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- AUXILIARY TEMPLATES -->
	
	<!-- Date format -->
	<xsl:template name="formatdate">
		<xsl:param name="datestr" />
		<xsl:variable name="sub">
			<xsl:value-of select="substring($datestr,1,10)" />
		</xsl:variable>
		<xsl:value-of select="$sub" />
	</xsl:template>
</xsl:stylesheet>