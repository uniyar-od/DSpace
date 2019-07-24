<?xml version="1.0" encoding="UTF-8" ?>
<!--
 
 
 The contents of this file are subject to the license and copyright
 detailed in the LICENSE and NOTICE files at the root of the source
 tree and available online at
 
 http://www.dspace.org/license/
 Developed by DSpace @ Lyncode <dspace@lyncode.com>
 
 > http://www.openarchives.org/OAI/2.0/oai_dc.xsd
 
 Global namespace:
 	oai_cerif		openaire tag
 	dc				used in oai_cerif:Publication
 	xsi				used in oai_cerif:Publication
 	
 Local Namespace:
 	oai_cerif:Publication	oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types"
 	oai_cerif:Publication	oai:Type scheme="https://w3id.org/cerif/vocab/OrganisationTypes"
 -->
<xsl:stylesheet
	xmlns:oai_cerif="https://www.openaire.eu/cerif-profile/1.1/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://www.lyncode.com/xoai"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd https://www.openaire.eu/cerif-profile/1.1/ https://www.openaire.eu/schema/cris/1.1/openaire-cerif-profile.xsd"
    version="1.0">
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />
    
    <xsl:key name="dc_relation_ispartof" match="doc:metadata/doc:element[@name='dc.relation.ispartof']" use="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element/doc:element/doc:field[@name = 'id']" />
    
    <xsl:key name="dc_contributor_author" match="doc:metadata/doc:element[@name='dc.contributor.author']" use="//doc:field[@name='id']" />
    <xsl:key name="dc_contributor_editor" match="doc:metadata/doc:element[@name='dc.contributor.editor']" use="//doc:field[@name='id']" />
    <xsl:key name="dc_publisher" match="doc:metadata/doc:element[@name='dc.publisher']" use="//doc:field[@name='id']" />
    <xsl:key name="dc_relation" match="doc:metadata/doc:element[@name='dc.relation']" use="//doc:field[@name='id']" />
    <xsl:key name="dc_relation_conference" match="doc:metadata/doc:element[@name='dc.relation.conference']" use="//doc:field[@name='id']" />
   
   	<!-- used when parsing Item -->
    <xsl:key name="affiliation.affiliationorgunit" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="crisou_parentorgunit__depth1" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="crisou_parentorgunit__depth2" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="crisou_parentorgunit__depth3" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="crisou_parentorgunit__depth4" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />

	<!-- used when parsing ou -->
	<xsl:key name="ou.parentorgunit__depth1" match="doc:metadata/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="ou.parentorgunit__depth2" match="doc:metadata/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="ou.parentorgunit__depth3" match="doc:metadata/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="ou.parentorgunit__depth4" match="doc:metadata/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />

	<!-- used when parsing rp -->
	<xsl:key name="rp.affiliation.affiliationorgunit" match="doc:metadata/doc:element[@name='affiliation.affiliationorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="rp.parentorgunit__depth1" match="doc:metadata/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="rp.parentorgunit__depth2" match="doc:metadata/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="rp.parentorgunit__depth3" match="doc:metadata/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="rp.parentorgunit__depth4" match="doc:metadata/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    		
    <!-- transate dc.type to Type xmlns="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types" -->
    <xsl:template name="oai_type">
        <xsl:param name="type" select="other"/>
        <xsl:choose>
            <!-- see driverDocumentTypeCondition, xaoi.xml -->
            
            <!-- journal article (http://purl.org/coar/resource_type/c_6501): An article on a particular topic and published in a journal issue. (adapted from fabio) -->
            <xsl:when test="$type='article' or $type='info:eu-repo/semantics/article'">http://purl.org/coar/resource_type/c_6501</xsl:when>
            <!-- bachelor thesis (http://purl.org/coar/resource_type/c_7a1f): A thesis reporting a research project undertaken as part of an undergraduate course of education leading to a bachelor’s degree. -->
            <xsl:when test="$type='bachelorthesis' or $type='info:eu-repo/semantics/bachelorthesis'">http://purl.org/coar/resource_type/c_46ec</xsl:when>
            <!-- master thesis (http://purl.org/coar/resource_type/c_bdcc): A thesis reporting a research project undertaken as part of a graduate course of education leading to a master’s degree. -->
            <xsl:when test="$type='masterthesis' or $type='info:eu-repo/semantics/masterthesis'">http://purl.org/coar/resource_type/c_db06</xsl:when>
            <!-- doctoral thesis (http://purl.org/coar/resource_type/c_db06): A thesis reporting the research undertaken during a period of graduate study leading to a doctoral degree -->
            <xsl:when test="$type='doctoralthesis' or $type='info:eu-repo/semantics/doctoralthesis'">http://purl.org/coar/resource_type/c_bdcc</xsl:when>
            <!-- book (http://purl.org/coar/resource_type/c_2f33): A non-serial publication that is complete in one volume or a designated finite number of volumes. (adapted from CiTO; EPrint Type vocabulary) -->
            <xsl:when test="$type='book' or $type='info:eu-repo/semantics/book'">http://purl.org/coar/resource_type/c_2f33</xsl:when>
            <!-- book part (http://purl.org/coar/resource_type/c_3248): A defined chapter or section of a book, usually with a separate title or number -->
            <xsl:when test="$type='bookpart' or $type='info:eu-repo/semantics/bookpart'">http://purl.org/coar/resource_type/c_3248</xsl:when>
            <!-- review (http://purl.org/coar/resource_type/c_efa0): A review of others’ published work. -->
            <xsl:when test="$type='review' or $type='info:eu-repo/semantics/review'">http://purl.org/coar/resource_type/c_efa0</xsl:when>
            <!-- conference object (http://purl.org/coar/resource_type/c_c94f): All kind of digital resources contributed to a conference, like conference presentation (slides), conference report, conference lecture, abstracts, demonstrations. For conference papers, posters or proceedings the specific concepts should be used. -->
            <xsl:when test="$type='conferenceobject' or $type='info:eu-repo/semantics/conferenceobject'">http://purl.org/coar/resource_type/c_c94f</xsl:when>
            <!-- lecture (http://purl.org/coar/resource_type/c_8544): A transcription of a talk delivered during an academic event. -->
            <xsl:when test="$type='lecture' or $type='info:eu-repo/semantics/lecture'">http://purl.org/coar/resource_type/c_8544</xsl:when>
            <!-- working paper (http://purl.org/coar/resource_type/c_8042): A working paper or preprint is a report on research that is still on-going or which has not yet been accepted for publication. -->
            <xsl:when test="$type='workingpaper' or $type='info:eu-repo/semantics/workingpaper'">http://purl.org/coar/resource_type/c_8042</xsl:when>
            <!-- preprint (http://purl.org/coar/resource_type/c_816b): Pre-print describes the first draft of the article - before peer-review, even before any contact with a publisher. This use is common amongst academics for whom the key modification of an article is the peer-review process... -->
            <xsl:when test="$type='preprint' or $type='info:eu-repo/semantics/preprint'">http://purl.org/coar/resource_type/c_816b</xsl:when>
            <!-- eport (http://purl.org/coar/resource_type/c_93fc): A report is a separately published record of research findings, research still in progress, or other technical findings, usually bearing a report number and sometimes a grant number assigned by the funding agency... -->
            <xsl:when test="$type='report' or $type='info:eu-repo/semantics/report'">http://purl.org/coar/resource_type/c_93fc</xsl:when>
            <!-- annotation (http://purl.org/coar/resource_type/c_1162): An annotation in the sense of a legal note is a legally explanatory comment on a decision handed down by a court or arbitral tribunal. -->
            <xsl:when test="$type='annotation' or $type='info:eu-repo/semantics/annotation'">http://purl.org/coar/resource_type/c_1162</xsl:when>
            <!-- contribution to journal (http://purl.org/coar/resource_type/c_3e5a): A contribution to a journal denotes a work published in a journal. If applicable sub-terms should be chosen. -->
            <xsl:when test="$type='contributiontoperiodical' or $type='info:eu-repo/semantics/contributiontoperiodical'">http://purl.org/coar/resource_type/c_3e5a</xsl:when>
            <!-- journal (http://purl.org/coar/resource_type/c_0640): A periodical of (academic) journal articles. (Adapted from bibo) -->
            <xsl:when test="$type='journal' or $type='info:eu-repo/semantics/journal'">http://purl.org/coar/resource_type/c_0640</xsl:when>
            <!-- TODO: review "patent" value -->
            
            <!-- other type of report (http://purl.org/coar/resource_type/c_18wq): Other types of report may include Business Plans Technical Specifications, data management plans, recommendation reports, white papers, ... -->
            <xsl:when test="$type='other' or $type='info:eu-repo/semantics/other'">http://purl.org/coar/resource_type/c_18wq</xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <!-- transate ou.type to Type xmlns="https://w3id.org/cerif/vocab/OrganisationTypes" -->
    <xsl:template name="oai_outype">
        <xsl:param name="type" select="'Academic Institute'"/>
        <xsl:choose>
            
            <!-- An academic institution is an educational institution dedicated to education and research, which grants academic degrees. -->
            <xsl:when test="$type='Academic Institute'">https://w3id.org/cerif/vocab/OrganisationTypes#AcademicInstitute</xsl:when>
            <!-- A university is an institution of higher education and research, which grants academic degrees in a variety of subjects. A university is a corporation that provides both undergraduate education and postgraduate education. -->
            <xsl:when test="$type='University'">https://w3id.org/cerif/vocab/OrganisationTypes#University</xsl:when>
    		<!-- The term "university college" is used in a number of countries to denote college institutions that provide tertiary education but do not have full or independent university status. A university college is often part of a larger university. The precise usage varies from country to country.. -->
            <xsl:when test="$type='University College'">https://w3id.org/cerif/vocab/OrganisationTypes#UniversityCollege</xsl:when>
    		<!-- A research institute is an establishment endowed for doing research. Research institutes may specialize in basic research or may be oriented to applied research. -->
    		<xsl:when test="$type='Research Institute'">https://w3id.org/cerif/vocab/OrganisationTypes#ResearchInstitute</xsl:when>
    		<!-- A strategic research institute's core mission is to provide analyses that respond to the needs of decision-makers. -->
    		<xsl:when test="$type='Strategic Research Insitute'">https://w3id.org/cerif/vocab/OrganisationTypes#StrategicResearchInsitute</xsl:when>
    		<!-- A company is a form of business organization. In the United States, a company is a corporation—or, less commonly, an association, partnership, or union—that carries on an industrial enterprise." Generally, a company may be a "corporation, partnership, association, joint-stock company, trust, fund, or organized group of persons, whether incorporated or not, and (in an official capacity) any receiver, trustee in bankruptcy, or similar official, or liquidating agent, for any of the foregoing." In English law, and therefore in the Commonwealth realms, a company is a form of body corporate or corporation, generally registered under the Companies Acts or similar legislation. It does not include a partnership or any other unincorporated group of persons. -->
    		<xsl:when test="$type='Company'">https://w3id.org/cerif/vocab/OrganisationTypes#Company</xsl:when>
    		<!-- Small and medium enterprises (also SMEs, small and medium businesses, SMBs, and variations thereof) are companies whose headcount or turnover falls below certain limits. EU Member States traditionally have their own definition of what constitutes an SME, for example the traditional definition in Germany had a limit of 250 employees, while, for example, in Belgium it could have been 100. But now the EU has started to standardize the concept. Its current definition categorizes companies with fewer than 10 employees as "micro", those with fewer than 50 employees as "small", and those with fewer than 250 as "medium". -->
    		<xsl:when test="$type='SME'">https://w3id.org/cerif/vocab/OrganisationTypes#SME</xsl:when>
    		<!-- A government is the organization, or agency through which a political unit exercises its authority, controls and administers public policy, and directs and controls the actions of its members or subjects. -->
    		<xsl:when test="$type='Government'">https://w3id.org/cerif/vocab/OrganisationTypes#Government</xsl:when>
    		<!-- Higher education or post-secondary education refers to a level of education that is provided at academies, universities, colleges, seminaries, institutes of technology, and certain other collegiate- level institutions, such as vocational schools, trade schools, and career colleges, that award academic degrees or professional certifications. -->
    		<xsl:when test="$type='Higher Education'">https://w3id.org/cerif/vocab/OrganisationTypes#HigherEducation</xsl:when>
    		<!-- An organization that is incorporated under state law and whose purpose is not to make profit, but rather to further a charitable, civic, scientific, or other lawful purpose. -->
    		<xsl:when test="$type='Private non-profit'">https://w3id.org/cerif/vocab/OrganisationTypes#Privatenon-profit</xsl:when>
    		<!-- An intergovernmental organization, sometimes rendered as an international governmental organization and both abbreviated as IGO, is an organization composed primarily of sovereign states (referred to as member states), or of other intergovernmental organizations. Intergovernmental organizations are often called international organizations, although that term may also include international nongovernmental organization such as international non-profit organizations or multinational corporations. -->
    		<xsl:when test="$type='Intergovernmental'">https://w3id.org/cerif/vocab/OrganisationTypes#Intergovernmental</xsl:when>
    		<!-- A charitable organization is a type of non-profit organization (NPO). It differs from other types of NPOs in that it centers on philanthropic goals (e.g. charitable, educational, religious, or other activities serving the public interest or common good). The legal definition of charitable organization (and of Charity) varies according to the country and in some instances the region of the country in which the charitable organization operates. The regulation, tax treatment, and the way in which charity law affects charitable organizations also varies. -->
    		<xsl:when test="$type='Charity'">https://w3id.org/cerif/vocab/OrganisationTypes#Charity</xsl:when>
    		<!-- Hospitals, trusts and other bodies receiving funding from central governement through the national insurance scheme. -->
    		<xsl:when test="$type='National Health Service'">https://w3id.org/cerif/vocab/OrganisationTypes#NationalHealthService</xsl:when>
    	</xsl:choose>
    </xsl:template>
    
    <!-- transate ou.type to Type xmlns="https://w3id.org/cerif/vocab/OrganisationTypes" -->
    <xsl:template name="oai_medium">
    	<xsl:param name="type" select="'Other'"/>
    	<xsl:choose>
    		<!-- Print (paper) -->
    		<xsl:when test="$type='Print'">http://issn.org/vocabularies/Medium#Print</xsl:when>
    		<!-- Online (online publication) -->
    		<xsl:when test="$type='Online'">http://issn.org/vocabularies/Medium#Online</xsl:when>
    		<!-- Digital carrier (CD-ROM, USB keys) -->
    		<xsl:when test="$type='Digital carrier'">http://issn.org/vocabularies/DigitalCarrier#Online</xsl:when>
    		<!-- Other (Loose-leaf publications, braille, etc.) -->
    		<xsl:when test="$type='Other'">http://issn.org/vocabularies/DigitalCarrier#Other</xsl:when>
    	</xsl:choose>
    </xsl:template>
    
    <!-- translate access to Type "http://purl.org/coar/access_right" -->
    <xsl:template name="oai_accesstype">
    	<xsl:param name="type" select="'open access'"/>
    	<xsl:choose>
    		<!-- Open access refers to a resource that is immediately and permanently online, and free for all on the Web, without financial and technical barriers. -->
    		<xsl:when test="$type='open access'">http://purl.org/coar/access_right/c_abf2</xsl:when>
    		<!-- Embargoed access refers to a resource that is metadata only access until released for open access on a certain date. Embargoes can be required by publishers and funders policies, or set by the author (e.g such as in the case of theses and dissertations). -->
    		<xsl:when test="$type='embargoed access'">http://purl.org/coar/access_right/c_f1cf</xsl:when>
    		<!-- Restricted access refers to a resource that is available in a system but with some type of restriction for full open access. This type of access can occur in a number of different situations. Some examples are described below: The user must log-in to the system in order to access the resource The user must send an email to the author or system administrator to access the resource Access to the resource is restricted to a specific community (e.g. limited to a university community) -->
    		<xsl:when test="$type='restricted access'">http://purl.org/coar/access_right/c_16ec</xsl:when>
    		<!-- Metadata only access refers to a resource in which access is limited to metadata only. The resource itself is described by the metadata, but is not directly available through the system or platform. This type of access can occur in a number of different situations. Some examples are described below: There is no electronic copy of the resource available (record links to a physical resource) The resource is only available elsewhere for a fee (record links to a subscription-based publisher version) The resource is available open access but at a different location (record links to a version at an open access publisher or archive) The resource is available elsewhere, but not in a fully open access format (record links to a read only, or other type of resources that is not permanent or in some way restricted) -->
    		<xsl:when test="$type='metadata only'">http://purl.org/coar/access_right/c_14cb</xsl:when>
    	</xsl:choose>
    </xsl:template>
    
    <!--	
    	oai_parentorgunit: Ricorsive template that handle orgunit.
    	
    	To add another level of recursion define crisou_parentorgunit__depthX, where X is the next level.
    	For instance copy crisou_parentorgunit__depth4 to crisou_parentorgunit__depth5 and add an estra level in
    	xpath of /doc:element[@name='crisou.parentorgunit'].
    	
    	Example of parameters:
	    	selector = doc:element[@name='crisou']/doc:element[@name='name']/doc:element
			id = $crisou_parentorgunit_id  
			uuid = $crisou_parentorgunit_uuid
			relid = $crisou_parentorgunit_reli
			key = 'crisou_parentorgunit_depth'
			depth = 1
    -->
	<xsl:template name="oai_parentorgunit" match="/">
		<xsl:param name="selector" />
		<xsl:param name="id" />
		<xsl:param name="uuid" />
		<xsl:param name="relid" />
		<xsl:param name="key" />
		<xsl:param name="depth" />
		
		<!-- grab name -->
		<xsl:for-each select="$selector">
			<!-- only value with relation equal to author uuid will be processed -->
			<xsl:if test="doc:field[@name='relid']/text()=$relid and doc:field[@name='id']/text()=$uuid">
			
			<oai_cerif:OrgUnit id="{$id}">
				<!-- duplicated (uo *) -->
				<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='type']/doc:element">
				<xsl:variable name="ou_type">
					<xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of>
				</xsl:variable>
				<oai_cerif:Type xmlns:oai="https://w3id.org/cerif/vocab/OrganisationTypes">
            		<xsl:call-template name="oai_outype"><xsl:with-param name="type" select="$ou_type" /></xsl:call-template>
            	</oai_cerif:Type>
            	</xsl:for-each>
            	
            	<!-- duplicated (uo *) -->
            	<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='acronym']/doc:element">
				<oai_cerif:Acronym><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:Acronym>
            	</xsl:for-each>
            	
				<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
			
				<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element">
				<oai_cerif:Identifier><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:Identifier>
            	</xsl:for-each>
            	
            	<!-- duplicated (uo *) -->
            	<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='email']/doc:element">
				<oai_cerif:ElectronicAddress><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:ElectronicAddress>
            	</xsl:for-each>
            	
				<!-- depth n-->
				<!--xsl:for-each select="/doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='crisou_parentorgunit__depth0']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element"-->
				<xsl:for-each select="../../../doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
				<xsl:variable name="parentorgunit_id">
					<xsl:value-of select="doc:field[@name='value']/text()" />
				</xsl:variable>
				<xsl:variable name="parentorgunit_uuid">
					<xsl:value-of select="doc:field[@name='id']/text()" />
				</xsl:variable>
				<xsl:variable name="parentorgunit_relid">
					<xsl:value-of select="doc:field[@name='relid']/text()" />
				</xsl:variable>
				<xsl:if test="$parentorgunit_relid=$uuid">

				<xsl:for-each select="key(concat($key, $depth), doc:field[@name='id']/text())">
					<!-- grab next id -->			    
					<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
						<xsl:if test="doc:field[@name='relid']/text()=$parentorgunit_relid and doc:field[@name='id']/text()=$parentorgunit_uuid">
							<xsl:variable name="relation_parentorgunit_id">
							<xsl:value-of select="doc:field[@name='value']/text()" />
							</xsl:variable>
							<xsl:variable name="relation_parentorgunit_uuid">
							<xsl:value-of select="doc:field[@name='id']/text()" />
							</xsl:variable>
							<xsl:variable name="relation_parentorgunit_relid">
							<xsl:value-of select="doc:field[@name='relid']/text()" />
							</xsl:variable>
						    
						    <!-- [DEBUG-deep]<xsl:value-of select="$depth + 1"></xsl:value-of> -->
						    <oai_cerif:Partof>
						        <xsl:call-template name="oai_parentorgunit">
						            <xsl:with-param name="selector" select="../../../../doc:element[@name='crisou']/doc:element[@name='name']/doc:element" />
						            <xsl:with-param name="id" select="$relation_parentorgunit_id" />
						            <xsl:with-param name="uuid" select="$relation_parentorgunit_uuid" />
						            <xsl:with-param name="relid" select="$relation_parentorgunit_relid" />
						            <xsl:with-param name="key" select="$key" />
						            <xsl:with-param name="depth" select="$depth + 1" />
						        </xsl:call-template>
						    </oai_cerif:Partof>
						</xsl:if>
					</xsl:for-each>
				
				</xsl:for-each>
				</xsl:if>
				</xsl:for-each>
			</oai_cerif:OrgUnit>
			
			</xsl:if>
		</xsl:for-each>
    </xsl:template>
    
    <!--	
    	oai_parentorgunit: Ricorsive template that handle orgunit.
    	
    	To add another level of recursion define crisou_parentorgunit__depthX, where X is the next level.
    	For instance copy crisou_parentorgunit__depth4 to crisou_parentorgunit__depth5 and add an estra level in
    	xpath of /doc:element[@name='crisou.parentorgunit'].
    	
    	Example of parameters:
			key = 'parentorgunit_depth'
			depth = 1
    -->
	<xsl:template name="oai_parentorgunit_root" match="/">
		<xsl:param name="key" />
		<xsl:param name="depth" />
		
		<xsl:variable name="id">
       		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
       			<xsl:value-of select="doc:field[@name='value']/text()" />
       		</xsl:for-each>
       	</xsl:variable>
        <xsl:variable name="uuid">
       		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element">
       			<xsl:value-of select="doc:field[@name='value']/text()" />
       		</xsl:for-each>
       	</xsl:variable>
       	
       	<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element">
			<oai_cerif:OrgUnit id="{$id}">
				<!-- duplicated (uo *) -->
				<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='type']/doc:element">
				<xsl:variable name="ou_type">
					<xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of>
				</xsl:variable>
				<oai_cerif:Type xmlns:oai="https://w3id.org/cerif/vocab/OrganisationTypes">
            		<xsl:call-template name="oai_outype"><xsl:with-param name="type" select="$ou_type" /></xsl:call-template>
            	</oai_cerif:Type>
            	</xsl:for-each>
            	
            	<!-- duplicated (uo *) -->
            	<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='acronym']/doc:element">
				<oai_cerif:Acronym><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:Acronym>
            	</xsl:for-each>
            	
				<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
            
            	<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element">
				<oai_cerif:Identifier><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:Identifier>
            	</xsl:for-each>
            	
            	<!-- duplicated (uo *) -->
            	<xsl:for-each select="../../../doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='email']/doc:element">
				<oai_cerif:ElectronicAddress><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:ElectronicAddress>
            	</xsl:for-each>
            	
				<!-- depth n-->
				<xsl:for-each select="../../../doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
				<xsl:variable name="parentorgunit_id">
					<xsl:value-of select="doc:field[@name='value']/text()" />
				</xsl:variable>
				<xsl:variable name="parentorgunit_uuid">
					<xsl:value-of select="doc:field[@name='id']/text()" />
				</xsl:variable>
				<xsl:variable name="parentorgunit_relid">
					<xsl:value-of select="doc:field[@name='relid']/text()" />
				</xsl:variable>
				<xsl:if test="$parentorgunit_relid=$uuid">
				<xsl:for-each select="key(concat($key, $depth), doc:field[@name='id']/text())">
					<!-- grab next id -->			    
					<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
						<xsl:if test="doc:field[@name='relid']/text()=$parentorgunit_relid and doc:field[@name='id']/text()=$parentorgunit_uuid">
							<xsl:variable name="relation_parentorgunit_id">
							<xsl:value-of select="doc:field[@name='value']/text()" />
							</xsl:variable>
							<xsl:variable name="relation_parentorgunit_uuid">
							<xsl:value-of select="doc:field[@name='id']/text()" />
							</xsl:variable>
							<xsl:variable name="relation_parentorgunit_relid">
							<xsl:value-of select="doc:field[@name='relid']/text()" />
							</xsl:variable>
						    
						    <!-- [DEBUG-deep]<xsl:value-of select="$depth + 1"></xsl:value-of> -->
						    <oai_cerif:Partof>
						        <xsl:call-template name="oai_parentorgunit">
						            <xsl:with-param name="selector" select="../../../../doc:element[@name='crisou']/doc:element[@name='name']/doc:element" />
						            <xsl:with-param name="id" select="$relation_parentorgunit_id" />
						            <xsl:with-param name="uuid" select="$relation_parentorgunit_uuid" />
						            <xsl:with-param name="relid" select="$relation_parentorgunit_relid" />
						            <xsl:with-param name="key" select="$key" />
						            <xsl:with-param name="depth" select="$depth + 1" />
						        </xsl:call-template>
						    </oai_cerif:Partof>
						</xsl:if>
					</xsl:for-each>
				
				</xsl:for-each>
				</xsl:if>
				</xsl:for-each>
			</oai_cerif:OrgUnit>
			
		</xsl:for-each>
    </xsl:template>

    <!--	
    	person: Template that handle Person.
    	
    	Example of parameters:
	    	author_crisitem_crisprop_id
	    	dc_contributor_id
	    	author_crisitem_crisprop_uuid
    -->
    <xsl:template name="person" match="/">
    	<xsl:param name="author_crisitem_crisprop_id" />
    	<xsl:param name="dc_contributor_id" />
    	<xsl:param name="author_crisitem_crisprop_uuid" />
    	<xsl:param name="key_affiliation_affiliationorgunit" />
    	<xsl:param name="crisou_parentorgunit__depth" />
    	
	    <oai_cerif:Person id="{$author_crisitem_crisprop_id}"> 
	        <oai_cerif:PersonName>
	        	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='familyname']/doc:element">
	            <xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:FamilyName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:FamilyName>
               	</xsl:if>
           		</xsl:for-each>
           					
           		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='firstname']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:PersonName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:PersonName>
               	</xsl:if>
           		</xsl:for-each>
	    	</oai_cerif:PersonName>
	    	
	    	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='gender']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:Gender><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Gender>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='rp']/doc:element[@name='orcid']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:ORCID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ORCID>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='alternativeORCID']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:AlternativeORCID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeORCID>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='rp']/doc:element[@name='authorid']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:ResearchID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ResearchID>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='alternativeResearchid']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:AlternativeResearchID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeResearchID>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='rp']/doc:element[@name='scopusid']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:ScopusAuthorID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ScopusAuthorID>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='alternativeScopus']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:AlternativeScopusAuthorID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeScopusAuthorID>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='isni']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:ISNI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ISNI>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='alternativeIsni']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:AlternativeISNI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeISNI>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='dai']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:DAI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:DAI>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='alternativeDAI']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:AlternativeDAI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeDAI>
               	</xsl:if>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='rp']/doc:element[@name='email']/doc:element">
               	<xsl:if test="not(doc:field[@name='id']) or (doc:field[@name='id']=$dc_contributor_id)">
               		<oai_cerif:ElectronicAddress><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ElectronicAddress>
               	</xsl:if>
           	</xsl:for-each>
	                    
		    <!-- oai_cerif:Affiliation [START] -->
			<xsl:for-each select="doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
				<xsl:variable name="affiliation.affiliationorgunit_id">
             		<xsl:value-of select="doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:variable name="affiliation.affiliationorgunit_uuid">
             		<xsl:value-of select="doc:field[@name='id']/text()" />
             	</xsl:variable>
             	<xsl:variable name="affiliation.affiliationorgunit_relid">
             		<xsl:value-of select="doc:field[@name='relid']/text()" />
             	</xsl:variable>
             			
               	<xsl:if test="$author_crisitem_crisprop_uuid=$affiliation.affiliationorgunit_relid">
		 		<xsl:for-each select="key($key_affiliation_affiliationorgunit, doc:field[@name='id']/text())">
				<!-- only value with relation id equals to author uuid -->
		        <oai_cerif:Affiliation>
		        	<!-- only value with relation equal to author uuid will be processed -->
					<xsl:call-template name="oai_parentorgunit">
						<xsl:with-param name="selector" select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element" />
						<xsl:with-param name="id" select="$affiliation.affiliationorgunit_id" />
						<xsl:with-param name="uuid" select="$affiliation.affiliationorgunit_uuid" />
						<xsl:with-param name="relid" select="$affiliation.affiliationorgunit_relid" />
						<xsl:with-param name="key" select="$crisou_parentorgunit__depth" />
						<xsl:with-param name="depth" select="1" />
					</xsl:call-template>
            	</oai_cerif:Affiliation>
	            </xsl:for-each>
           		</xsl:if>
           					                
	      	</xsl:for-each>
	        <!-- oai_cerif:Affiliation [END] -->
		</oai_cerif:Person>
    </xsl:template>
    
	<!--	
    	oai_contributors: Template that handle Authors/Editors/Publishers.
    	
    	Example of parameters:
	    	dc_contributor_id
    -->
	<xsl:template name="oai_contributors" match="/">
		<xsl:param name="dc_contributor_id" />
		
    	<!-- oai_cerif:Authors, DisplayName -->
   		<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='fullname']/doc:element">
   			<xsl:if test="doc:field[@name='id']=$dc_contributor_id">
         		<oai_cerif:DisplayName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:DisplayName>
       		</xsl:if>
        </xsl:for-each>

		<xsl:variable name="author_crisitem_crisprop_id">
		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
 			<xsl:if test="doc:field[@name='id']=$dc_contributor_id">
 				<xsl:value-of select="doc:field[@name='value']" />
	        </xsl:if>
	    </xsl:for-each>
	    </xsl:variable>
	      			
	    <xsl:variable name="author_crisitem_crisprop_uuid">
		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element">
 			<xsl:if test="doc:field[@name='id']=$dc_contributor_id">
 				<xsl:value-of select="doc:field[@name='value']" />
	        </xsl:if>
	    </xsl:for-each>
	    </xsl:variable>
	 	               	
		<!-- Person call -->
		<xsl:call-template name="person">
			<xsl:with-param name="author_crisitem_crisprop_id" select="$author_crisitem_crisprop_uuid" />
			<xsl:with-param name="dc_contributor_id" select="$dc_contributor_id" />
			<xsl:with-param name="author_crisitem_crisprop_uuid" select="$author_crisitem_crisprop_uuid" />
			<xsl:with-param name="key_affiliation_affiliationorgunit" select="'affiliation.affiliationorgunit'" />
			<xsl:with-param name="crisou_parentorgunit__depth" select="'crisou_parentorgunit__depth'" />
		</xsl:call-template>
					
    </xsl:template>
    
    <!--	
    	project: Template that handle Project.
    	
    	Example of parameters:
	    	dc_relation_id
    -->
	<xsl:template name="project" match="/">
		<xsl:param name="selector" />
		<xsl:param name="dc_relation_id" />
            
        <xsl:for-each select="$selector">
        <oai_cerif:Project id="$dc_relation_id">
        
        	<!--  MISSING METADATA FIX [START]: type, acronym -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='type']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Type><xsl:value-of select="." /></oai_cerif:Type>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='acronym']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Acronym><xsl:value-of select="." /></oai_cerif:Acronym>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Identifier><xsl:value-of select="." /></oai_cerif:Identifier>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='startdate']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:StartDate><xsl:value-of select="." /></oai_cerif:StartDate>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='enddate']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:EndDate><xsl:value-of select="." /></oai_cerif:EndDate>
	        </xsl:for-each>
	        
	        <!--  MISSING METADATA FIX [START]: consortium, team, funded  -->
	        <!-- Missing: Coordinator, Partner, Contractor, InKindContributor, Member -->
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='organization']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Consortium><xsl:value-of select="." /></oai_cerif:Consortium>
	        </xsl:for-each>
	        
	        <!-- Missing: PrincipalInvestigator, Contact, Member -->
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='principalinvestigator']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Team><xsl:value-of select="." /></oai_cerif:Team>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='funder']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Funded>
	        		<!-- Missing: OrgUnit, Person, DisplayName -->
	        		<xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='funder']/doc:element/doc:field[@name='value']">
	        		<oai_cerif:By></oai_cerif:By>
	        		</xsl:for-each>
	        		
	        		<!-- Missing: Funding -->
	        		<xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='fundingprogram']/doc:element/doc:field[@name='value']">
	        		<oai_cerif:As></oai_cerif:As>
	        		</xsl:for-each>
	        	</oai_cerif:Funded>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='description']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='keywords']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keyword><xsl:value-of select="." /></oai_cerif:Keyword>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Abstract><xsl:value-of select="." /></oai_cerif:Abstract>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='status']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Status><xsl:value-of select="." /></oai_cerif:Status>
	        </xsl:for-each>
	        
	        <!--  MISSING METADATA FIX [START]: uses, OAMandate -->
	        <!-- Missing Equipment -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uses']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Uses><xsl:value-of select="." /></oai_cerif:Uses>
	        </xsl:for-each>
	        <!-- Missing mandated, uri -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='oamandate']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:OAMandate><xsl:value-of select="." /></oai_cerif:OAMandate>
	        </xsl:for-each>
	        
        </oai_cerif:Project>
        </xsl:for-each>
    </xsl:template>
    
    <!--	
    	event: Template that handle Event.
    	
    	Example of parameters:
	    	dc_relation_id
    -->
	<xsl:template name="events" match="/">
		<xsl:param name="selector" />
		<xsl:param name="dc_relation_conference_id" />
            
        <xsl:for-each select="$selector">
        <oai_cerif:Event id="{$dc_relation_conference_id}">
       		<!--  MISSING METADATA FIX [START]: type, acronym -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='type']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Type><xsl:value-of select="." /></oai_cerif:Type>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='acronym']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Acronym><xsl:value-of select="." /></oai_cerif:Acronym>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsname']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Name><xsl:value-of select="." /></oai_cerif:Name>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventslocation']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Place><xsl:value-of select="." /></oai_cerif:Place>
	        </xsl:for-each>
	        
	        <!--  MISSING METADATA FIX [START]: country -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='country']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Country><xsl:value-of select="." /></oai_cerif:Country>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsstartdate']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:StartDate><xsl:value-of select="." /></oai_cerif:StartDate>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsenddate']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:EndDate><xsl:value-of select="." /></oai_cerif:EndDate>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsinformation']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Description><xsl:value-of select="." /></oai_cerif:Description>
	        </xsl:for-each>
	        
	        <!--  MISSING METADATA FIX [START]: subject, keywords -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='keywords']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keywords><xsl:value-of select="." /></oai_cerif:Keywords>>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
	        
	        <!--  MISSING METADATA FIX [START]: organizer, sponsor, partner  -->
	        <!-- Missing: OrgUnit or Project -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='organizer']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Organizer><xsl:value-of select="." /></oai_cerif:Organizer>
	        </xsl:for-each>
	        
	        <!-- Missing: OrgUnit or Project -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='sponsor']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Sponsor><xsl:value-of select="." /></oai_cerif:Sponsor>
	        </xsl:for-each>
	        
	        <!-- Missing: OrgUnit or Project -->
	        <xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='partner']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Partner><xsl:value-of select="." /></oai_cerif:Partner>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
        </oai_cerif:Event>
        </xsl:for-each>
	</xsl:template>
	
    <!--	
    	publisher: Template that handle Authors/Editors/Publishers.
    	
    	Example of parameters:
	    	dc_contributor_id
    -->
	<xsl:template name="publication" match="/">
       <xsl:variable name="item_prop_id">
            <xsl:value-of select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
        </xsl:variable>
        
        <oai_cerif:Publication id="{$item_prop_id}">
            <!-- dc.type BUG: one /doc:element removed -->
            <xsl:variable name="dc_type_ci">
                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
            </xsl:variable>
            <xsl:variable name="dc_type" select="translate($dc_type_ci,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
            
            <oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
            	<xsl:call-template name="oai_type"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template>
            </oai:Type>
            
            <!-- dc.title (BUG, missing one /doc:element)-->
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
                <oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
            </xsl:for-each>
            
            <!--  MISSING METADATA FIX [START]: subtitle -->
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element[@name='alternative']/doc:element/doc:field[@name='value']">
                <oai_cerif:Subtitle><xsl:value-of select="." /></oai_cerif:Subtitle>
            </xsl:for-each>
            <!-- MISSING METADATA FIX [END] -->
            
            <!-- oai_cerif:PublishedIn [START] -->
            <xsl:for-each select="doc:metadata/doc:element[@name='dc.relation.ispartof']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
            	<oai_cerif:PublishedIn>

				<xsl:variable name="dc_relation_ispartof_id">
             		<xsl:value-of select="doc:field[@name='id']/text()" />
             	</xsl:variable>
            	<xsl:for-each select="key('dc_relation_ispartof', doc:field[@name='id']/text())">
            	
            		<xsl:variable name="ispartof_crisitem_crisprop_id">
            			<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
            			<xsl:if test="doc:field[@name='id']=$dc_relation_ispartof_id">
                        	<xsl:value-of select="doc:field[@name='value']" />
                        </xsl:if>
                        </xsl:for-each>
                    </xsl:variable>
                    
                    <xsl:variable name="crisitem_crisvprop_type">
                        <xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
                    </xsl:variable>
                    <oai_cerif:Publication id="{$ispartof_crisitem_crisprop_id}">
                        <oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
                            <xsl:call-template name="oai_type"><xsl:with-param name="type" select="$crisitem_crisvprop_type" /></xsl:call-template>
                        </oai:Type>
                    
	            		<!-- oai_cerif:PublishedIn, Title (crisjournals.journalsname) --> 
	                    <xsl:for-each select="doc:element[@name='crisjournals']/doc:element[@name='journalsname']/doc:element">
	                    	<xsl:if test="doc:field[@name='id']=$dc_relation_ispartof_id">
	                        <oai_cerif:Title><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Title>
	                        </xsl:if>
	                    </xsl:for-each>
	                    
	                    <!-- oai_cerif:PublishedIn, ISSN -->
	                    <xsl:for-each select="doc:element[@name='crisjournals']/doc:element[@name='journalsissn']/doc:element">
	                    	<xsl:if test="doc:field[@name='id']=$dc_relation_ispartof_id">
	                        <oai_cerif:ISSN><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ISSN>
	                        </xsl:if>
	                    </xsl:for-each>
                	</oai_cerif:Publication>
                	
				</xsl:for-each>
				
                </oai_cerif:PublishedIn>
            </xsl:for-each>
            <!-- oai_cerif:PublishedIn [END] -->
            
             <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
                <oai_cerif:PublicationDate><xsl:value-of select="." /></oai_cerif:PublicationDate>
            </xsl:for-each>
            
            <!--  MISSING METADATA FIX [START]: number, volume, issue, edition, startpage, endpage -->
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='number']/doc:element/doc:field[@name='value']">
                <oai_cerif:Number><xsl:value-of select="." /></oai_cerif:Number>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='volume']/doc:element/doc:field[@name='value']">
                <oai_cerif:Volume><xsl:value-of select="." /></oai_cerif:Volume>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='issue']/doc:element/doc:field[@name='value']">
                <oai_cerif:Issue><xsl:value-of select="." /></oai_cerif:Issue>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='issue']/doc:element/doc:field[@name='value']">
                <oai_cerif:Edition><xsl:value-of select="." /></oai_cerif:Edition>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='startpage']/doc:element/doc:field[@name='value']">
                <oai_cerif:StartPage><xsl:value-of select="." /></oai_cerif:StartPage>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='endpage']/doc:element/doc:field[@name='value']">
                <oai_cerif:EndPage><xsl:value-of select="." /></oai_cerif:EndPage>
            </xsl:for-each>
            <!-- MISSING METADATA FIX [END] -->
            
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                <oai_cerif:DOI><xsl:value-of select="." /></oai_cerif:DOI>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='handle']/doc:element/doc:field[@name='value']">
                <oai_cerif:Handle><xsl:value-of select="." /></oai_cerif:Handle>
            </xsl:for-each>
            
            <!--  MISSING METADATA FIX [START]: pmcid, isinumber, scpnumber -->
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='pmid']/doc:element/doc:field[@name='value']">
                <oai_cerif:PMCID><xsl:value-of select="." /></oai_cerif:PMCID>
            </xsl:for-each>

			<xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isi']/doc:element/doc:field[@name='value']">
                <oai_cerif:ISI-Number><xsl:value-of select="." /></oai_cerif:ISI-Number>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='scpnumber']/doc:element/doc:field[@name='value']">
                <oai_cerif:SCP-Number><xsl:value-of select="." /></oai_cerif:SCP-Number>
            </xsl:for-each>
            <!-- MISSING METADATA FIX [END] -->
            
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='issn']/doc:element/doc:field[@name='value']">
                <oai_cerif:ISSN><xsl:value-of select="." /></oai_cerif:ISSN>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isbn']/doc:element/doc:field[@name='value']">
                <oai_cerif:ISBN><xsl:value-of select="." /></oai_cerif:ISBN>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='url']/doc:element/doc:field[@name='value']">
                <oai_cerif:URL><xsl:value-of select="." /></oai_cerif:URL>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
                <oai_cerif:URN><xsl:value-of select="." /></oai_cerif:URN>
            </xsl:for-each>
            
            <!-- oai_cerif:Authors [START] -->
            <oai_cerif:Authors>
            <xsl:for-each select="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
             	<xsl:variable name="dc_contributor_author_id">
             		<xsl:value-of select="doc:field[@name='id']/text()" />
             	</xsl:variable>
            	<xsl:for-each select="key('dc_contributor_author', doc:field[@name='id']/text())">
            	    <oai_cerif:Author>
						<xsl:call-template name="oai_contributors">
							<xsl:with-param name="dc_contributor_id" select="$dc_contributor_author_id" />
						</xsl:call-template>
					</oai_cerif:Author>
				</xsl:for-each>
            </xsl:for-each>
           	</oai_cerif:Authors>
            <!-- oai_cerif:Authors [END] -->
            
            <!-- oai_cerif:Editors [START] -->
            <oai_cerif:Editors>
            <xsl:for-each select="doc:metadata/doc:element[@name='dc.contributor.editor']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
             	<xsl:variable name="dc_contributor_editor_id">
             		<xsl:value-of select="doc:field[@name='id']/text()" />
             	</xsl:variable>
            	<xsl:for-each select="key('dc_contributor_editor', doc:field[@name='id']/text())">
            	    <oai_cerif:Editor>
						<xsl:call-template name="oai_contributors">
							<xsl:with-param name="dc_contributor_id" select="$dc_contributor_editor_id" />
						</xsl:call-template>
					</oai_cerif:Editor>
				</xsl:for-each>
            </xsl:for-each>
           	</oai_cerif:Editors>
            <!-- oai_cerif:Editors [END] -->
            
            <!-- oai_cerif:Publishers [START] -->
            <oai_cerif:Publishers>
            <xsl:for-each select="doc:metadata/doc:element[@name='dc.contributor.editor']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
             	<xsl:variable name="dc_publisher_id">
             		<xsl:value-of select="doc:field[@name='id']/text()" />
             	</xsl:variable>
            	<xsl:for-each select="key('dc_publisher', doc:field[@name='id']/text())">
           	    <oai_cerif:Publisher>
					<xsl:call-template name="oai_contributors">
						<xsl:with-param name="dc_contributor_id" select="$dc_publisher_id" />
					</xsl:call-template>
				</oai_cerif:Publisher>
				</xsl:for-each>
            </xsl:for-each>
           	</oai_cerif:Publishers>
            <!-- oai_cerif:Publishers [END] -->
            
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='license']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:License><xsl:value-of select="." /></oai_cerif:License>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <!--  MISSING METADATA FIX [START]: keywords -->
	        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='keywords']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keywords><xsl:value-of select="." /></oai_cerif:Keywords>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Abstract><xsl:value-of select="." /></oai_cerif:Abstract>
	        </xsl:for-each>
	        
	        <!--  MISSING METADATA FIX [START]: status -->
	        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='status']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Status><xsl:value-of select="." /></oai_cerif:Status>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='dc.relation']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
	       	<oai_cerif:OriginatesFrom>
	        	<xsl:variable name="dc_relation_id">
             		<xsl:value-of select="doc:field[@name='value']/text()" />
             	</xsl:variable>
            	<xsl:for-each select="key('dc_relation', doc:field[@name='id']/text())">
					<xsl:call-template name="project">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="dc_relation_id" select="$dc_relation_id" />
					</xsl:call-template>
            	</xsl:for-each>
	        </oai_cerif:OriginatesFrom>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='dc.relation.conference']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
	        <oai_cerif:PresentedAt>
	        	<xsl:variable name="dc_relation_conference_id">
             		<xsl:value-of select="doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:for-each select="key('dc_relation_conference', doc:field[@name='id']/text())">
             	<xsl:call-template name="events">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="dc_relation_conference_id" select="$dc_relation_conference_id" />
					</xsl:call-template>
            	</xsl:for-each>
	        </oai_cerif:PresentedAt>
	       	</xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='outputfrom']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:OutputFrom><xsl:value-of select="." /></oai_cerif:OutputFrom>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='coverage']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Coverage><xsl:value-of select="." /></oai_cerif:Coverage>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='reference']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:References><xsl:value-of select="." /></oai_cerif:References>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='access']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Access><xsl:value-of select="." /></oai_cerif:Access>
	        </xsl:for-each>
	        <!-- MISSING METADATA FIX [END] -->
        </oai_cerif:Publication>
	</xsl:template>
	
    <xsl:template match="/">
        <!-- NEW: virtuals medatata
         crisitem.crisvprop.id        	(the id of the cris item)
         crisitem.crisvprop.uuid      	(the uuid of the cris item)
         crisitem.crisvprop.handle    	(the handle of the cris item)
         crisitem.crisvprop.fullname	(map the attribute fullname, supports two formats: familyname, firstname or firstname familyname)
         crisitem.crisvprop.familyname	(the family name)
         crisitem.crisvprop.firstname	(the first name)
         crisitem.crisvprop.objecttype	(the type: project, rp, ou, journals, ...)
         
         item.vprop.id					(the id of the item)
         item.vprop.handle				(the handle of the item)
         item.vprop.objecttype			(the vaue is always item)
         -->
         
        <!-- item -->
        <xsl:if test="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='item'">
        	<xsl:call-template name="publication" />
        </xsl:if>
        
        <!-- ou -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='ou'">
        	<xsl:for-each select="doc:metadata">
	        	<xsl:call-template name="oai_parentorgunit_root">
					<xsl:with-param name="key" select="'ou.parentorgunit__depth'" />
					<xsl:with-param name="depth" select="1" />
				</xsl:call-template>
			</xsl:for-each>
        </xsl:if>
        
        <!-- rp -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='rp'">
			<xsl:for-each select="doc:metadata">
				<xsl:variable name="author_crisitem_crisprop_id">
				<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
		 			<xsl:value-of select="doc:field[@name='value']" />
			    </xsl:for-each>
			    </xsl:variable>
		      			
			    <xsl:variable name="author_crisitem_crisprop_uuid">
				<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element">
		 			<xsl:value-of select="doc:field[@name='value']" />
			    </xsl:for-each>
			    </xsl:variable>
		 	               	
				<!-- Person call -->
				<xsl:call-template name="person">
					<xsl:with-param name="author_crisitem_crisprop_id" select="$author_crisitem_crisprop_id" />
					<xsl:with-param name="dc_contributor_id" select="$author_crisitem_crisprop_uuid" />
					<xsl:with-param name="author_crisitem_crisprop_uuid" select="$author_crisitem_crisprop_uuid" />
					<xsl:with-param name="key_affiliation_affiliationorgunit" select="'rp.affiliation.affiliationorgunit'" />
					<xsl:with-param name="crisou_parentorgunit__depth" select="'rp.parentorgunit__depth'" />
				</xsl:call-template>
			</xsl:for-each>
        </xsl:if>
        
        <!-- project -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='project'">
        	<xsl:for-each select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
	        	<xsl:variable name="dc_relation_id">
             		<xsl:value-of select="doc:field[@name='value']/text()" />
             	</xsl:variable>
 				<xsl:call-template name="project">
					<xsl:with-param name="selector" select="../../../.." />
					<xsl:with-param name="dc_relation_id" select="$dc_relation_id" />
				</xsl:call-template>
	        </xsl:for-each>
        </xsl:if>
        
        <!-- event -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='events'">
        	<xsl:for-each select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
	        	<xsl:variable name="dc_relation_conference_id">
             		<xsl:value-of select="doc:field[@name='value']/text()" />
             	</xsl:variable>
 				<xsl:call-template name="events">
					<xsl:with-param name="selector" select="../../../.." />
					<xsl:with-param name="dc_relation_id" select="$dc_relation_conference_id" />
				</xsl:call-template>
	        </xsl:for-each>
        </xsl:if>
        
    </xsl:template>

</xsl:stylesheet>