<?xml version="1.0" encoding="UTF-8" ?>
<!--
 
 
 The contents of this file are subject to the license and copyright
 detailed in the LICENSE and NOTICE files at the root of the source
 tree and available online at
 
 http://www.dspace.org/license/
 Developed by DSpace @ Lyncode <dspace@lyncode.com>
 
 > http://www.openarchives.org/OAI/2.0/oai_cerif.xsd
 
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
            <!-- [NOTE] default is other -->
            <xsl:otherwise>http://purl.org/coar/resource_type/c_18wq</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="oai_producttype">
        <xsl:param name="type" select="other"/>
        <xsl:choose>
	        <!-- interactive resource (http://purl.org/coar/resource_type/c_e9a0): A resource requiring interaction from the user to be understood, executed, or experienced. Examples include forms on Web pages, applets, multimedia learning objects, chat services, or virtual reality environments. -->
	        <xsl:when test="$type='interactive resource'">http://purl.org/coar/resource_type/c_e9a0</xsl:when>
	        <!-- website (http://purl.org/coar/resource_type/c_7ad9): A website, also written as web site or simply site, is a set of related web pages typically served from a single web domain -->
	        <xsl:when test="$type='website'">http://purl.org/coar/resource_type/c_7ad9</xsl:when>
	        <!-- dataset (http://purl.org/coar/resource_type/c_ddb1): A collection of related facts and data encoded in a defined structure. (adapted from fabio; DataCite) -->
	        <xsl:when test="$type='dataset'">http://purl.org/coar/resource_type/c_ddb1</xsl:when>
	        <!-- image (http://purl.org/coar/resource_type/c_c513): A visual representation other than text, including all types of moving image and still image -->
	        <xsl:when test="$type='image'">http://purl.org/coar/resource_type/c_c513</xsl:when>
	        <!-- moving image (http://purl.org/coar/resource_type/c_8a7e): A moving display, either generated dynamically by a computer program or formed from a series of pre-recorded still images imparting an impression of motion when shown in succession. -->
	        <xsl:when test="$type='moving image'">http://purl.org/coar/resource_type/c_8a7e</xsl:when>
	        <!-- video (http://purl.org/coar/resource_type/c_12ce): A recording of visual images, usually in motion and with sound accompaniment. -->
	        <xsl:when test="$type='video'">http://purl.org/coar/resource_type/c_12ce</xsl:when>
	        <!-- still image (http://purl.org/coar/resource_type/c_ecc8): A recorded static visual representation. -->
	        <xsl:when test="$type='still image'">http://purl.org/coar/resource_type/c_ecc8</xsl:when>
	        <!-- software (http://purl.org/coar/resource_type/c_5ce6): A computer program in source code (text) or compiled form. -->
	        <xsl:when test="$type='software'">http://purl.org/coar/resource_type/c_5ce6</xsl:when>
	        <!-- workflow (http://purl.org/coar/resource_type/c_393c): A recorded sequence of connected steps, which may be automated, specifying a reliably repeatable sequence of operations... -->
	        <xsl:when test="$type='workflow'">http://purl.org/coar/resource_type/c_393c</xsl:when>
	        <!-- cartographic material (http://purl.org/coar/resource_type/c_12cc): Any material representing the whole or part of the earth or any celestial body at any scale.  -->
	        <xsl:when test="$type='cartographic material'">http://purl.org/coar/resource_type/c_12cc</xsl:when>
	        <!-- map (http://purl.org/coar/resource_type/c_12cd): Defined as a representation normally to scale and on a flat medium, of a selection of material or abstract features on... -->
	        <xsl:when test="$type='map'">http://purl.org/coar/resource_type/c_12cd</xsl:when>
	        <!-- sound (http://purl.org/coar/resource_type/c_18cc): A resource primarily intended to be heard.  -->
	        <xsl:when test="$type='sound'">http://purl.org/coar/resource_type/c_18cc</xsl:when>
	        
	        <!-- [NOTE] Other value is reserved for publication, using "other product" -->
	        <!-- other (http://purl.org/coar/resource_type/c_1843): A rest category which may cover text, interactive, sound, or image-based resources not explicitly addressed in any concept in this vocabulary -->
	        <xsl:when test="$type='other product'">http://purl.org/coar/resource_type/c_1843</xsl:when>
		</xsl:choose>
  	</xsl:template>
  	
  	 <xsl:template name="oai_patenttype">
        <xsl:param name="type" select="other"/>
        <xsl:choose>
	        <!-- patent (http://purl.org/coar/resource_type/c_15cd): A patent or patent application. -->
	        <xsl:when test="$type='patent'">http://purl.org/coar/resource_type/c_15cd</xsl:when>
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
    
    <!-- transate funding.type to Type xmlns="https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types" -->
    <xsl:template name="oai_foundingtype">
        <xsl:param name="type" select="'Academic Institute'"/>
        <xsl:choose>
    		<!-- Funding Programme (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#FundingProgramme): A funding programme or a similar scheme that funds some number of proposals. -->
    		<xsl:when test="$type='Funding Programme'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#FundingProgramme</xsl:when>
    		<!-- Call (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Call): Call for proposals: a specific campaign for the funder to solicit proposals from interested researchers and institutions -->
    		<xsl:when test="$type='Call'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Call</xsl:when>
    		<!-- Tender (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Tender): Tender for services or deliveries: a specific campaign for the funder to solicit offers for services or deliveries. -->
    		<xsl:when test="$type='Tender'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Tender</xsl:when>
    		<!-- Gift (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Gift): A donation connected with specific terms and conditions. -->
    		<xsl:when test="$type='Gift'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Gift</xsl:when>
    		<!-- Internal Funding (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#InternalFunding): Internal funds used to amend or replace external funding. -->
    		<xsl:when test="$type='Internal Funding'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#InternalFunding</xsl:when>
    		<!-- Contract (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Contract) -->
    		<xsl:when test="$type='Contract'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Contract</xsl:when>
    		<!-- Award (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Award): -->
    		<xsl:when test="$type='Award'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Award</xsl:when>
    		<!-- Grant (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Grant): -->
    		<xsl:when test="$type='Grant'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Funding_Types#Grant</xsl:when>
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
    
	<xsl:template name="oai_compatibility">
    	<xsl:param name="type" select="'OpenAIRE Guidelines 1.1 compatible CRIS'"/>
    	<xsl:choose>
    		<!-- OpenAIRE Guidelines 1.1 compatible CRIS (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Service_Compatibility#1.1): CRIS compatible with OpenAIRE Guidelines for CRIS managers version 1.1 -->
    		<xsl:when test="$type='OpenAIRE Guidelines 1.1 compatible CRIS'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Service_Compatibility#1.1</xsl:when>
    		<!-- OpenAIRE Guidelines 1.0 compatible CRIS (https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Service_Compatibility#1.0): CRIS compatible with OpenAIRE Guidelines for CRIS managers version 1.0 -->
    		<xsl:when test="$type='OpenAIRE Guidelines 1.0 compatible CRIS'">https://www.openaire.eu/cerif-profile/vocab/OpenAIRE_Service_Compatibility#1.0</xsl:when>
    	</xsl:choose>
    </xsl:template>
    
    <!--	
    	oai_parentorgunit: Ricorsive template that handle orgunit.
    	
    	To add another level of recursion define crisou_parentorgunit__depthX, where X is the next level.
    	For instance copy crisou_parentorgunit__depth4 to crisou_parentorgunit__depth5 and add an estra level in
    	xpath of /doc:element[@name='authority'].
    	
    	Example of parameters:
	    	selector = doc:element[@name='crisou']/doc:element[@name='name']/doc:element
			id = $crisou_parentorgunit_id  
			depth = 1
    -->
	<xsl:template name="oai_parentorgunit" match="/">
		<xsl:param name="selector" />
		<xsl:param name="id" />
		<xsl:param name="depth" />
		
		<!-- grab name -->
		<xsl:for-each select="$selector">
			<oai_cerif:OrgUnit id="{$id}">
				<!-- [DEBUG: outype]<xsl:value-of select="doc:element[@name='crisou']/doc:element[@name='outype']/doc:element/doc:element/doc:field[@name='value']/text()"></xsl:value-of>-->
				<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='outype']/doc:element/doc:element">
				<xsl:variable name="ou_type">
					<xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of>
				</xsl:variable>
				<oai_cerif:Type xmlns:oai="https://w3id.org/cerif/vocab/OrganisationTypes">
            		<xsl:call-template name="oai_outype"><xsl:with-param name="type" select="$ou_type" /></xsl:call-template>
            	</oai_cerif:Type>
            	</xsl:for-each>
            	
            	<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='acronym']/doc:element/doc:element">
				<oai_cerif:Acronym><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:Acronym>
            	</xsl:for-each>
            	
            	<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element/doc:element">
				<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
				</xsl:for-each>
			
				<xsl:variable name="typeid" select="doc:element[@name='ouidentifier']/doc:element[@name='ouidentifiertypeid']/doc:element/doc:element/doc:field[@name='value']" />
				<xsl:variable name="id" select="doc:element[@name='ouidentifier']/doc:element[@name='ouidentifierid']/doc:element/doc:element/doc:field[@name='value']" />
				<xsl:if test="$typeid">
				<oai_cerif:Identifier type="{$typeid}"><xsl:value-of select="$id"/></oai_cerif:Identifier>
				</xsl:if>
            	
            	<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='email']/doc:element/doc:element">
				<oai_cerif:ElectronicAddress><xsl:value-of select="doc:field[@name='value']/text()"></xsl:value-of></oai_cerif:ElectronicAddress>
            	</xsl:for-each>
            	
				<!-- depth n-->
				<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='parentorgunit']/doc:element/doc:element/doc:element[@name='authority']">				
				<!-- TODO: remove?
				<xsl:variable name="parentorgunit_id">
					<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
				</xsl:variable>
				<xsl:variable name="parentorgunit_uuid">
					<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
				</xsl:variable>
				<xsl:variable name="parentorgunit_relid">
					<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='relid']/text()" />
				</xsl:variable>
				-->
				
				<!-- grab next id -->			    
				<xsl:variable name="relation_parentorgunit_id">
				<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
				</xsl:variable>
				<!-- TODO: remove? 
				<xsl:variable name="relation_parentorgunit_uuid">
				<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
				</xsl:variable>
				<xsl:variable name="relation_parentorgunit_relid">
				<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='relid']/text()" />
				</xsl:variable>
				-->
			    <oai_cerif:Partof>
			        <xsl:call-template name="oai_parentorgunit">
			            <xsl:with-param name="selector" select="." />
			            <xsl:with-param name="id" select="$relation_parentorgunit_id" />
			            <xsl:with-param name="depth" select="$depth + 1" />
			        </xsl:call-template>
			    </oai_cerif:Partof>
				</xsl:for-each>

			</oai_cerif:OrgUnit>
		</xsl:for-each>
    </xsl:template>

    <!--	
    	person: Template that handle Person.
    	
    	TODO: handle selector parameter and set the value to . when calling
    			the "person" template.
    	
    	Example of parameters:
	    	person_id
    -->
    <xsl:template name="person" match="/">
    	<xsl:param name="person_id" />
    	
	    <oai_cerif:Person id="{$person_id}"> 
	        <oai_cerif:PersonName>
	        	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='familyname']/doc:element">
               		<oai_cerif:FamilyName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:FamilyName>
           		</xsl:for-each>

           		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='firstname']/doc:element">
               		<oai_cerif:PersonName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:PersonName>
           		</xsl:for-each>
 
 				<!-- only if familyname and firstname are empty -->
 				<xsl:if test="not(normalize-space(doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='familyname']/doc:element/doc:field[@name='value'])) and not(normalize-space(doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='firstname']/doc:element/doc:field[@name='value']))">
          		<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='fullName']/doc:element/doc:element">
               		<oai_cerif:OtherNames><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:OtherNames>
           		</xsl:for-each>
           		</xsl:if>
	    	</oai_cerif:PersonName>
	    	
	    	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='gender']/doc:element/doc:element">
               	<oai_cerif:Gender><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Gender>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='orcid']/doc:element/doc:element">
               	<oai_cerif:ORCID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ORCID>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='orcidAlternative']/doc:element/doc:element">
               	<oai_cerif:AlternativeORCID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeORCID>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='authorid']/doc:element/doc:element">
               	<oai_cerif:ResearchID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ResearchID>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='authoridAlternative']/doc:element/doc:element">
               	<oai_cerif:AlternativeResearchID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeResearchID>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='scopusid']/doc:element/doc:element">
               	<oai_cerif:ScopusAuthorID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ScopusAuthorID>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='scopusidAlternative']/doc:element/doc:element">
             	<oai_cerif:AlternativeScopusAuthorID><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeScopusAuthorID>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='isni']/doc:element/doc:element">
               	<oai_cerif:ISNI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ISNI>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='isniAlternative']/doc:element/doc:element">
               	<oai_cerif:AlternativeISNI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeISNI>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='dai']/doc:element/doc:element">
               	<oai_cerif:DAI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:DAI>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='daiAlternative']/doc:element/doc:element">
               	<oai_cerif:AlternativeDAI><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:AlternativeDAI>
           	</xsl:for-each>
           	
           	<xsl:for-each select="doc:element[@name='rp']/doc:element[@name='email']/doc:element">
               	<oai_cerif:ElectronicAddress><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ElectronicAddress>
           	</xsl:for-each>
	                    
		    <!-- oai_cerif:Affiliation [START] -->
			<xsl:for-each select="doc:element[@name='affiliation']/doc:element[@name='affiliationorgunit']/doc:element/doc:element/doc:element[@name='authority']">
			<xsl:if test="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
				<xsl:variable name="affiliation.affiliationorgunit_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<!--  TODO: remove
             	<xsl:variable name="affiliation.affiliationorgunit_uuid">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
             	<xsl:variable name="affiliation.affiliationorgunit_relid">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='relid']/text()" />
             	</xsl:variable>
             	-->
             			
		        <oai_cerif:Affiliation>
		        	<!-- only value with relation equal to author uuid will be processed -->
					<xsl:call-template name="oai_parentorgunit">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="id" select="$affiliation.affiliationorgunit_id" />
						<xsl:with-param name="depth" select="1" />
					</xsl:call-template>
            	</oai_cerif:Affiliation>
           				
           	</xsl:if>	                
	      	</xsl:for-each>
	        <!-- oai_cerif:Affiliation [END] -->
		</oai_cerif:Person>
    </xsl:template>
    
    <!--	
    	project: Template that handle Project.
    	
    	Example of parameters:
    		selector
	    	dc_relation_id
    -->
	<xsl:template name="project" match="/">
		<xsl:param name="selector" />
		<xsl:param name="dc_relation_id" />
            
        <xsl:for-each select="$selector">
        <oai_cerif:Project id="{$dc_relation_id}">
        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='projecttype']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Type><xsl:value-of select="." /></oai_cerif:Type>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='projectacronym']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Acronym><xsl:value-of select="." /></oai_cerif:Acronym>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='title']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
	        </xsl:for-each>
	        
	        <xsl:variable name="typeid" select="doc:element[@name='projectidentifier']/doc:element[@name='projectidentifiertypeid']/doc:element/doc:element/doc:field[@name='value']" />
				<xsl:variable name="id" select="doc:element[@name='projectidentifier']/doc:element[@name='projectidentifierid']/doc:element/doc:element/doc:field[@name='value']" />
				<xsl:if test="$typeid">
				<oai_cerif:Identifier type="{$typeid}"><xsl:value-of select="$id"/></oai_cerif:Identifier>
			</xsl:if>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='startdate']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:StartDate><xsl:value-of select="." /></oai_cerif:StartDate>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='expdate']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:EndDate><xsl:value-of select="." /></oai_cerif:EndDate>
	        </xsl:for-each>
	        
	        <!-- Consortium [START] -->
	        <oai_cerif:Consortium>
	        <!-- Coordinator -->
            <xsl:for-each select="doc:element[@name='projectconsortium']/doc:element[@name='projectconsortiumcoordinator']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="coordinator_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:Coordinator>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="$coordinator_id" />
					</xsl:call-template>
           		</oai_cerif:Coordinator>
            </xsl:for-each>
            <!-- Partner -->
            <xsl:for-each select="doc:element[@name='projectconsortium']/doc:element[@name='projectconsortiumpartner']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="partner_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:Partner>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="$partner_id" />
					</xsl:call-template>
           		</oai_cerif:Partner>
            </xsl:for-each>
            <!-- Contractor -->
	        <xsl:for-each select="doc:element[@name='projectconsortium']/doc:element[@name='projectconsortiumcontactor']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="contractor_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:Contractor>
					<xsl:call-template name="oai_parentorgunit">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="id" select="$contractor_id" />
						<xsl:with-param name="depth" select="1" />
					</xsl:call-template>
           		</oai_cerif:Contractor>
            </xsl:for-each>
            <!-- organization -->
	        <xsl:for-each select="doc:element[@name='projectconsortium']/doc:element[@name='projectconsortiumorganization']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="organization_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:Contractor>
            		<xsl:call-template name="oai_parentorgunit">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="id" select="$organization_id" />
						<xsl:with-param name="depth" select="1" />
					</xsl:call-template>
           		</oai_cerif:Contractor>
            </xsl:for-each>
            <!-- InKindContributor -->
            <xsl:for-each select="doc:element[@name='projectconsortium']/doc:element[@name='projectconsortiuminkindcontributor']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="partner_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:InKindContributor>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="$partner_id" />
					</xsl:call-template>
           		</oai_cerif:InKindContributor>
            </xsl:for-each>
	        </oai_cerif:Consortium>
	        <!-- Consortium [END] -->
	        
	        <!-- Team [START] -->
	        <oai_cerif:Team>
	        <!-- PrincipalInvestigator -->
	        <xsl:for-each select="doc:element[@name='projectteam']/doc:element[@name='projectteamprincipalinvestigator']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="principalinvestigator_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:PrincipalInvestigator>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="$principalinvestigator_id" />
					</xsl:call-template>
           		</oai_cerif:PrincipalInvestigator>
            </xsl:for-each>
            <!-- Contact -->
            <xsl:for-each select="doc:element[@name='projectteam']/doc:element[@name='projectteamcontact']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="contact_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:Contact>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="$contact_id" />
					</xsl:call-template>
           		</oai_cerif:Contact>
            </xsl:for-each>
            <!-- Member -->
            <xsl:for-each select="doc:element[@name='projectteam']/doc:element[@name='projectteammember']/doc:element/doc:element/doc:element[@name='authority']">
             	<xsl:variable name="member_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:Member>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="$member_id" />
					</xsl:call-template>
           		</oai_cerif:Member>
            </xsl:for-each>
	        </oai_cerif:Team>
	        <!-- Team [END] -->
	        
	        <!-- Funded [START] -->
	        <oai_cerif:Funded>
	        <!-- By [Note: relation crisproject.funder must be enabled and person template have to be improved to handle ou relation ]-->
        	<xsl:for-each select="doc:element[@name='projectfunded']/doc:element[@name='projectfundedby']/doc:element/doc:element/doc:element[@name='authority']">
        		<xsl:variable name="funderby_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	<oai_cerif:By>
					<xsl:call-template name="person">
						<xsl:with-param name="person_id" select="funderby_id" />
					</xsl:call-template>
           		</oai_cerif:By>
        	</xsl:for-each>
        	<!-- As (TODO: handle relation with funder) -->
        	<xsl:for-each select="doc:element[@name='projectfunded']/doc:element[@name='projectfundedas']/doc:element/doc:element/doc:element[@name='authority']">
        		<xsl:variable name="funderas_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
        		<oai_cerif:As>
        		</oai_cerif:As>
        	</xsl:for-each>
	        </oai_cerif:Funded>
	        <!-- Funded [END] -->
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='subject']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='keywords']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keyword><xsl:value-of select="." /></oai_cerif:Keyword>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='abstract']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Abstract><xsl:value-of select="." /></oai_cerif:Abstract>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='status']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Status><xsl:value-of select="." /></oai_cerif:Status>
	        </xsl:for-each>
	        
	        <!--  REVIEW METADATA [START]: uses, oamandate -->
	        <xsl:for-each select="doc:element[@name='crisproject']/doc:element[@name='uses']/doc:element/doc:element/doc:field[@name='value']">
	        	<xsl:variable name="equipment_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
	        	<oai_cerif:Uses>
	        		<xsl:call-template name="equipment">
	        			<xsl:with-param name="selector" select="." />
						<xsl:with-param name="equipment_id" select="$equipment_id" />
					</xsl:call-template>
	        	</oai_cerif:Uses>
	        </xsl:for-each>
	        <!-- TODO: review uri values? -->
	        <xsl:variable name="typeid" select="doc:element[@name='projectoamadate']/doc:element[@name='projectoamadatemandated']/doc:element/doc:element/doc:field[@name='value']" />
				<xsl:variable name="uri" select="doc:element[@name='projectoamadate']/doc:element[@name='projectoamadateuri']/doc:element/doc:element/doc:field[@name='value']" />
				<xsl:if test="$typeid">
				<oai_cerif:OAMandate mandated="{$typeid}" uri="{$uri}"/>
			</xsl:if>
	        <!--  REVIEW METADATA [END] -->
	        
        </oai_cerif:Project>
        </xsl:for-each>
    </xsl:template>
    
    <!--	
    	event: Template that handle Event.
    	
    	Example of parameters:
    		selector
	    	event_id
    -->
	<xsl:template name="events" match="/">
		<xsl:param name="selector" />
		<xsl:param name="event_id" />
            
        <xsl:for-each select="$selector">
        <oai_cerif:Event id="{$event_id}">
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventstype']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Type><xsl:value-of select="." /></oai_cerif:Type>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsacronym']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Acronym><xsl:value-of select="." /></oai_cerif:Acronym>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsname']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Name><xsl:value-of select="." /></oai_cerif:Name>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventslocation']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Place><xsl:value-of select="." /></oai_cerif:Place>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsiso-country']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Country><xsl:value-of select="." /></oai_cerif:Country>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsstartdate']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:StartDate><xsl:value-of select="." /></oai_cerif:StartDate>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsenddate']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:EndDate><xsl:value-of select="." /></oai_cerif:EndDate>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsdescription']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Description><xsl:value-of select="." /></oai_cerif:Description>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventssubject']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventskeywords']/doc:element/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keywords><xsl:value-of select="." /></oai_cerif:Keywords>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventsorganizer']/doc:element/doc:element/doc:element[@name='authority']">
	        	<oai_cerif:Organizer>
	        		<xsl:variable name="organizer_id">
             			<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             		</xsl:variable>
                    <xsl:call-template name="project_ou_chooser">
            			<xsl:with-param name="selector" select="." />
            			<xsl:with-param name="project_ou_id" select="$organizer_id" />
            		</xsl:call-template>
	        	</oai_cerif:Organizer>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventssponsor']/doc:element/doc:element/doc:element[@name='authority']">
	        	<oai_cerif:Sponsor>
	        		<xsl:variable name="sponsor_id">
             			<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             		</xsl:variable>
                    <xsl:call-template name="project_ou_chooser">
            			<xsl:with-param name="selector" select="." />
            			<xsl:with-param name="project_ou_id" select="$sponsor_id" />
            		</xsl:call-template>
	        	</oai_cerif:Sponsor>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='crisevents']/doc:element[@name='eventspartner']/doc:element/doc:element/doc:element[@name='authority']">
	        	<oai_cerif:Partner>
	        		<xsl:variable name="partner_id">
             			<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             		</xsl:variable>
                    <xsl:call-template name="project_ou_chooser">
            			<xsl:with-param name="selector" select="." />
            			<xsl:with-param name="project_ou_id" select="$partner_id" />
            		</xsl:call-template>
	        	</oai_cerif:Partner>
	        </xsl:for-each>
        </oai_cerif:Event>
        </xsl:for-each>
	</xsl:template>
	
	 <!--	
    	equipment: Template that handle Equipment.
    	
    	Example of parameters:
    		selector
	    	equipment_id
    -->
	<xsl:template name="equipment" match="/">
		<xsl:param name="selector" />
		<xsl:param name="equipment_id" />
            
        <xsl:for-each select="$selector">
        <oai_cerif:Equipment id="{$equipment_id}">
        	<!-- TODO: define -->
        </oai_cerif:Equipment>
        </xsl:for-each>
   	</xsl:template>
	
	<!-- 
		publication_chooser: Template that handle publication using a cris journal or a pubblication item.
	
    	Example of parameters:
	    	selector
	    	publication_id
	 -->
	<xsl:template name="publication_chooser" match="/">
		<xsl:param name="selector" />
		<xsl:param name="publication_id" />
		
		<xsl:for-each select="$selector">
		
		<!-- journals -->
        <xsl:if test="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='journals'">
			<xsl:call-template name="publication_journal">
				<xsl:with-param name="selector" select="$selector" />
				<xsl:with-param name="publication_id" select="$publication_id" />
			</xsl:call-template>
        </xsl:if>
        
        <!-- item -->
        <xsl:if test="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='item'">
        	<xsl:call-template name="publication">
        		<xsl:with-param name="selector" select="$selector" />
        	</xsl:call-template>
        </xsl:if>
        
        </xsl:for-each>
	</xsl:template>
	
	<!-- 
		rp_ou_chooser: Template that handle research profile/organization unit.
		
		TODO: test
		
    	Example of parameters:
	    	selector
	    	rp_ou_id
	 -->
	<xsl:template name="rp_ou_chooser" match="/">
		<xsl:param name="selector" />
		<xsl:param name="rp_ou_id" />
		
		<xsl:for-each select="$selector">
		
		<!-- rp -->
        <xsl:if test="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='rp'">
			<xsl:call-template name="person">
				<!-- TODO: add selector to person_id  
				<xsl:with-param name="selector" select="$selector" />
				-->
				<xsl:with-param name="person_id" select="$rp_ou_id" />
			</xsl:call-template>
        </xsl:if>
        
        <!-- ou -->
        <xsl:if test="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='ou'">
        	<xsl:call-template name="oai_parentorgunit">
		    	<xsl:with-param name="selector" select="$selector" />
		        <xsl:with-param name="id" select="$rp_ou_id" />
		        <xsl:with-param name="depth" select="1" />
		   	</xsl:call-template>
        </xsl:if>
        
        </xsl:for-each>
	</xsl:template>
	
	<!-- 
		project_ou_chooser: Template that handle project/organization unit.
	
    	Example of parameters:
	    	selector
	    	project_ou_id
	 -->
	<xsl:template name="project_ou_chooser" match="/">
		<xsl:param name="selector" />
		<xsl:param name="project_ou_id" />
		
		<xsl:for-each select="$selector">
		
		<!-- project -->
        <xsl:if test="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='project'">
			<xsl:call-template name="project">
				<xsl:with-param name="selector" select="$selector" />
				<xsl:with-param name="dc_relation_id" select="$project_ou_id" />
			</xsl:call-template>
        </xsl:if>
        
        <!-- ou -->
        <xsl:if test="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='ou'">
        	<xsl:call-template name="oai_parentorgunit">
		    	<xsl:with-param name="selector" select="$selector" />
		        <xsl:with-param name="id" select="$project_ou_id" />
		        <xsl:with-param name="depth" select="1" />
		   	</xsl:call-template>
        </xsl:if>
        
        </xsl:for-each>
	</xsl:template>
	
	<!-- 
		item_chooser: Template that handle publication using an item selected among pubblications, products, patents. 
	
    	Example of parameters:
	    	selector
	 -->
	<xsl:template name="item_chooser" match="/">
		<xsl:param name="selector" />
		
		<xsl:for-each select="$selector">
			
			<!-- check type (is a publication or a product or ...) -->
        	<xsl:variable name="dc_type_ci">
                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
            </xsl:variable>
            <xsl:variable name="dc_type" select="translate($dc_type_ci,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
            
            <!-- check product -->
            <xsl:variable name="type_product"><xsl:call-template name="oai_producttype"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template></xsl:variable>
            <!-- check patent  -->
            <xsl:variable name="type_patent"><xsl:call-template name="oai_patenttype"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template></xsl:variable>
            <!-- check publication -->
            <xsl:variable name="type_publication"><xsl:call-template name="oai_type"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template></xsl:variable>
            
            <xsl:choose>
            	<xsl:when test="$type_product != ''">
            		[TODO:]<xsl:value-of select="type_product"></xsl:value-of> <xsl:value-of select="$type_product"></xsl:value-of>
            	</xsl:when>
            	<xsl:when test="$type_patent != ''">
            		[TODO:]<xsl:value-of select="type_patent"></xsl:value-of> <xsl:value-of select="$type_patent"></xsl:value-of>
            	</xsl:when>
            	<xsl:when test="$type_publication != ''">
            		<!-- [Note] $type_publication is always not empty.  -->
            		<xsl:call-template name="publication">
		        		<xsl:with-param name="selector" select="doc:metadata" />
		        	</xsl:call-template>
            	</xsl:when>
            </xsl:choose>
            
		</xsl:for-each>
	</xsl:template>
	
	<!-- 
		publication_journal: Template that handle publication using a journal
	
    	Example of parameters:
	    	selector
	    	publication_id
	 -->
	 <xsl:template name="publication_journal" match="/">
		<xsl:param name="selector" />
		<xsl:param name="publication_id" />
		
		<xsl:for-each select="$selector">
		<xsl:variable name="crisitem_crisvprop_type">
        	<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
        </xsl:variable>
                    
        <oai_cerif:Publication id="{$publication_id}">
               <oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
                   <xsl:call-template name="oai_type"><xsl:with-param name="type" select="$crisitem_crisvprop_type" /></xsl:call-template>
               </oai:Type>
           
    		<!-- oai_cerif:PublishedIn, Title (crisjournals.journalsname) --> 
            <xsl:for-each select="doc:element[@name='crisjournals']/doc:element[@name='journalsname']/doc:element">
                <oai_cerif:Title><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Title>
            </xsl:for-each>
            
            <!-- oai_cerif:PublishedIn, ISSN -->
            <xsl:for-each select="doc:element[@name='crisjournals']/doc:element[@name='journalsissn']/doc:element">
                <oai_cerif:ISSN><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:ISSN>
            </xsl:for-each>
       	</oai_cerif:Publication>
   		</xsl:for-each>
   	</xsl:template>
                	
    <!--	
    	publication: Template that handle publication using a publication item
    	
    	Example of parameters:
	    	selector
    -->
	<xsl:template name="publication" match="/">
		<xsl:param name="selector" />
							
		<xsl:for-each select="$selector">
       	<xsl:variable name="item_prop_id">
            <xsl:value-of select="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
        </xsl:variable>
        
        <oai_cerif:Publication id="{$item_prop_id}">
            <xsl:variable name="dc_type_ci">
                <xsl:value-of select="doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
            </xsl:variable>
            <xsl:variable name="dc_type" select="translate($dc_type_ci,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
            
            <oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
            	<xsl:call-template name="oai_type"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template>
            </oai:Type>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
                <oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='title']/doc:element[@name='alternative']/doc:element/doc:field[@name='value']">
                <oai_cerif:Subtitle><xsl:value-of select="." /></oai_cerif:Subtitle>
            </xsl:for-each>
            
            <!-- oai_cerif:PublishedIn [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='publication']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:PublishedIn>

            		<xsl:variable name="publication_id">
                        <xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
                    </xsl:variable>

                    <xsl:call-template name="publication_chooser">
            			<xsl:with-param name="selector" select="." />
            			<xsl:with-param name="publication_id" select="$publication_id" />
            		</xsl:call-template>

                </oai_cerif:PublishedIn>
            </xsl:for-each>
            <!-- oai_cerif:PublishedIn [END] -->
            
            <!-- oai_cerif:PartOf [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartof']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:PartOf>
            		<xsl:call-template name="publication">
            			<xsl:with-param name="selector" select="." />
            		</xsl:call-template>
            	</oai_cerif:PartOf>
           	</xsl:for-each>
            <!-- oai_cerif:PartOf [END] -->
            
             <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
                <oai_cerif:PublicationDate><xsl:value-of select="." /></oai_cerif:PublicationDate>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='number']/doc:element/doc:field[@name='value']">
                <oai_cerif:Number><xsl:value-of select="." /></oai_cerif:Number>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='volume']/doc:element/doc:field[@name='value']">
                <oai_cerif:Volume><xsl:value-of select="." /></oai_cerif:Volume>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
                <oai_cerif:Issue><xsl:value-of select="." /></oai_cerif:Issue>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='edition']/doc:element/doc:field[@name='value']">
                <oai_cerif:Edition><xsl:value-of select="." /></oai_cerif:Edition>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='startpage']/doc:element/doc:field[@name='value']">
                <oai_cerif:StartPage><xsl:value-of select="." /></oai_cerif:StartPage>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='endpage']/doc:element/doc:field[@name='value']">
                <oai_cerif:EndPage><xsl:value-of select="." /></oai_cerif:EndPage>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                <oai_cerif:DOI><xsl:value-of select="." /></oai_cerif:DOI>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='handle']/doc:element/doc:field[@name='value']">
                <oai_cerif:Handle><xsl:value-of select="." /></oai_cerif:Handle>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='pmid']/doc:element/doc:field[@name='value']">
                <oai_cerif:PMCID><xsl:value-of select="." /></oai_cerif:PMCID>
            </xsl:for-each>

			<xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isi']/doc:element/doc:field[@name='value']">
                <oai_cerif:ISI-Number><xsl:value-of select="." /></oai_cerif:ISI-Number>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='scp']/doc:element/doc:field[@name='value']">
                <oai_cerif:SCP-Number><xsl:value-of select="." /></oai_cerif:SCP-Number>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='issn']/doc:element/doc:field[@name='value']">
                <oai_cerif:ISSN><xsl:value-of select="." /></oai_cerif:ISSN>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='isbn']/doc:element/doc:field[@name='value']">
                <oai_cerif:ISBN><xsl:value-of select="." /></oai_cerif:ISBN>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='url']/doc:element/doc:field[@name='value']">
                <oai_cerif:URL><xsl:value-of select="." /></oai_cerif:URL>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
                <oai_cerif:URN><xsl:value-of select="." /></oai_cerif:URN>
            </xsl:for-each>
            
            <!-- oai_cerif:Authors [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='author']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Authors>
             	<xsl:variable name="dc_contributor_author_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
             	</xsl:variable>
            	    <oai_cerif:Author>
 						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_contributor_author_id" />
						</xsl:call-template>
					</oai_cerif:Author>
           		</oai_cerif:Authors>
            </xsl:for-each>
            <!-- oai_cerif:Authors [END] -->
            
            <!-- oai_cerif:Editors [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='contributor']/doc:element[@name='editor']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Editors>
             	<xsl:variable name="dc_contributor_editor_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
            	    <oai_cerif:Editor>
						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_contributor_editor_id" />
						</xsl:call-template>
					</oai_cerif:Editor>
           		</oai_cerif:Editors>
            </xsl:for-each>
            <!-- oai_cerif:Editors [END] -->
            
            <!-- oai_cerif:Publishers [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Publishers>
             	<xsl:variable name="dc_publisher_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
           	    	<oai_cerif:Publisher>
						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_publisher_id" />
						</xsl:call-template>
					</oai_cerif:Publisher>
           		</oai_cerif:Publishers>
            </xsl:for-each>
            <!-- oai_cerif:Publishers [END] -->
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='license']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:License><xsl:value-of select="." /></oai_cerif:License>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='keywords']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keywords><xsl:value-of select="." /></oai_cerif:Keywords>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Abstract><xsl:value-of select="." /></oai_cerif:Abstract>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='status']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Status><xsl:value-of select="." /></oai_cerif:Status>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:element[@name='authority']">
	       	<oai_cerif:OriginatesFrom>
	        	<xsl:variable name="dc_relation_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
 					<xsl:call-template name="project">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="dc_relation_id" select="$dc_relation_id" />
					</xsl:call-template>
 	        </oai_cerif:OriginatesFrom>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='conference']/doc:element/doc:element[@name='authority']">
	        <oai_cerif:PresentedAt>
	        	<xsl:variable name="dc_relation_conference_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:call-template name="events">
					<xsl:with-param name="selector" select="." />
					<xsl:with-param name="event_id" select="$dc_relation_conference_id" />
				</xsl:call-template>
 	        </oai_cerif:PresentedAt>
	       	</xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isbasedon']/doc:element/doc:element[@name='authority']">
	        <oai_cerif:OutputFrom>
	        	<xsl:variable name="outputfrom_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:call-template name="events">
					<xsl:with-param name="selector" select="." />
					<xsl:with-param name="event_id" select="$outputfrom_id" />
				</xsl:call-template>
	     	</oai_cerif:OutputFrom>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='coverage']/doc:element[@name='temporal']/doc:element/doc:element[@name='authority']">
	        <oai_cerif:Coverage>
	        	<xsl:variable name="coverage_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:call-template name="events">
					<xsl:with-param name="selector" select="." />
					<xsl:with-param name="event_id" select="$coverage_id" />
				</xsl:call-template>
	        </oai_cerif:Coverage>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isreferencedby']/doc:element/doc:element[@name='authority']">
	        	<oai_cerif:References>
             		<xsl:call-template name="item_chooser">
						<xsl:with-param name="selector" select="." />
					</xsl:call-template>
	        	</oai_cerif:References>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Access><xsl:value-of select="." /></oai_cerif:Access>
	        </xsl:for-each>
        </oai_cerif:Publication>
        </xsl:for-each>
	</xsl:template>
	
	<!--	
    	product: Template that handle publication using a product item
    	
    	TODO: add product_id parameter. 
    	
    	Example of parameters:
	    	selector
    -->
	<xsl:template name="product" match="/">
		<xsl:param name="selector" />
							
		<xsl:for-each select="$selector">
       	<xsl:variable name="item_prop_id">
            <xsl:value-of select="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
        </xsl:variable>
        
        <oai_cerif:Product id="{$item_prop_id}">
            <xsl:variable name="dc_type_ci">
                <xsl:value-of select="doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
            </xsl:variable>
            <xsl:variable name="dc_type" select="translate($dc_type_ci,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
            
            <oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
            	<xsl:call-template name="oai_producttype"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template>
            </oai:Type>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
                <oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='version']/doc:element/doc:field[@name='value']">
                <oai_cerif:VersionInfo><xsl:value-of select="." /></oai_cerif:VersionInfo>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='ark']/doc:element/doc:field[@name='value']">
                <oai_cerif:ARK><xsl:value-of select="." /></oai_cerif:ARK>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='doi']/doc:element/doc:field[@name='value']">
                <oai_cerif:DOI><xsl:value-of select="." /></oai_cerif:DOI>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='handle']/doc:element/doc:field[@name='value']">
                <oai_cerif:Handle><xsl:value-of select="." /></oai_cerif:Handle>
            </xsl:for-each>
            
			<xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='url']/doc:element/doc:field[@name='value']">
                <oai_cerif:URL><xsl:value-of select="." /></oai_cerif:URL>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element[@name='uri']/doc:element/doc:field[@name='value']">
                <oai_cerif:URN><xsl:value-of select="." /></oai_cerif:URN>
            </xsl:for-each>
            
            <!-- oai_cerif:Creators [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='creator']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Creators>
             	<xsl:variable name="dc_creator_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
            	    <oai_cerif:Creator>
						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_creator_id" />
						</xsl:call-template>
					</oai_cerif:Creator>
           		</oai_cerif:Creators>
            </xsl:for-each>
            <!-- oai_cerif:Creators [END] -->
            
            <!-- oai_cerif:Publishers [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='publisher']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Publishers>
             	<xsl:variable name="dc_publisher_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
           	    	<oai_cerif:Publisher>
						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_publisher_id" />
						</xsl:call-template>
					</oai_cerif:Publisher>
           		</oai_cerif:Publishers>
            </xsl:for-each>
            <!-- oai_cerif:Publishers [END] -->
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='rights']/doc:element[@name='license']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:License><xsl:value-of select="." /></oai_cerif:License>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Description><xsl:value-of select="." /></oai_cerif:Description>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='keywords']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keywords><xsl:value-of select="." /></oai_cerif:Keywords>
	        </xsl:for-each>
            
            <!-- oai_cerif:PartOf [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='ispartof']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:PartOf>
             		<xsl:call-template name="item_chooser">
						<xsl:with-param name="selector" select="." />
					</xsl:call-template>
            	</oai_cerif:PartOf>
           	</xsl:for-each>
            <!-- oai_cerif:PartOf [END] -->
            
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:element[@name='authority']">
	       	<oai_cerif:OriginatesFrom>
	        	<xsl:variable name="dc_relation_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
 					<xsl:call-template name="project">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="dc_relation_id" select="$dc_relation_id" />
					</xsl:call-template>
 	        </oai_cerif:OriginatesFrom>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isbasedon']/doc:element/doc:element[@name='authority']">
	       	<oai_cerif:GeneratedBy>
	        	<xsl:variable name="generatedby_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
 					<xsl:call-template name="equipment">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="equipment_id" select="$generatedby_id" />
					</xsl:call-template>
 	        </oai_cerif:GeneratedBy>
	        </xsl:for-each>
	        
	       	<xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='conference']/doc:element/doc:element[@name='authority']">
	        <oai_cerif:PresentedAt>
	        	<xsl:variable name="dc_relation_conference_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:call-template name="events">
					<xsl:with-param name="selector" select="." />
					<xsl:with-param name="event_id" select="$dc_relation_conference_id" />
				</xsl:call-template>
 	        </oai_cerif:PresentedAt>
	       	</xsl:for-each>
	       	
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='coverage']/doc:element[@name='temporal']/doc:element/doc:element[@name='authority']">
	        <oai_cerif:Coverage>
	        	<xsl:variable name="coverage_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
             	<xsl:call-template name="events">
					<xsl:with-param name="selector" select="." />
					<xsl:with-param name="event_id" select="$coverage_id" />
				</xsl:call-template>
	        </oai_cerif:Coverage>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isreferencedby']/doc:element/doc:element[@name='authority']">
	        	<oai_cerif:References>
             		<xsl:call-template name="item_chooser">
						<xsl:with-param name="selector" select="." />
					</xsl:call-template>
	        	</oai_cerif:References>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='rights']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Access><xsl:value-of select="." /></oai_cerif:Access>
	        </xsl:for-each>
        </oai_cerif:Product>
        </xsl:for-each>
	</xsl:template>
	
	<!--	
    	patent: Template that handle publication using a patent item
    	
    	Example of parameters:
	    	selector
    -->
	<xsl:template name="patent" match="/">
		<xsl:param name="selector" />
							
		<xsl:for-each select="$selector">
       	<xsl:variable name="item_prop_id">
            <xsl:value-of select="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
        </xsl:variable>
        
        <oai_cerif:Patent id="{$item_prop_id}">
            <xsl:variable name="dc_type_ci">
                <xsl:value-of select="doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
            </xsl:variable>
            <xsl:variable name="dc_type" select="translate($dc_type_ci,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
            
            <oai:Type xmlns:oai="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
            	<xsl:call-template name="oai_patenttype"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template>
            </oai:Type>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
                <oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='decsription']/doc:element[@name='version']/doc:element/doc:field[@name='value']">
                <oai_cerif:VersionInfo><xsl:value-of select="." /></oai_cerif:VersionInfo>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='created']/doc:element/doc:field[@name='value']">
                <oai_cerif:RegistrationDate><xsl:value-of select="." /></oai_cerif:RegistrationDate>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field[@name='value']">
                <oai_cerif:ApprovalDate><xsl:value-of select="." /></oai_cerif:ApprovalDate>
            </xsl:for-each>
            
            <!-- TODO: read countrycode from iso-language -->
            <xsl:for-each select="doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='countrycode']/doc:element/doc:field[@name='value']">
                <oai_cerif:CountryCode><xsl:value-of select="." /></oai_cerif:CountryCode>
            </xsl:for-each>
            
            <!-- TODO: handle realtion with OrgUnit -->
            <xsl:for-each select="doc:element[@name='dcterms']/doc:element[@name='publisher']/doc:element/doc:field[@name='value']">
                <oai_cerif:Issuer><xsl:value-of select="." /></oai_cerif:Issuer>
            </xsl:for-each>
            
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='identifier']/doc:element/doc:field[@name='value']">
                <oai_cerif:PatentNumber><xsl:value-of select="." /></oai_cerif:PatentNumber>
            </xsl:for-each>
            
            <!-- oai_cerif:Inventors [START] -->
            <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='creator']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Inventors>
             	<xsl:variable name="dc_creator_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
            	    <oai_cerif:Inventor>
						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_creator_id" />
						</xsl:call-template>
					</oai_cerif:Inventor>
           		</oai_cerif:Inventors>
            </xsl:for-each>
            <!-- oai_cerif:Inventors [END] -->
            
            <!-- oai_cerif:Holders [START] -->
            <xsl:for-each select="doc:element[@name='dcterms']/doc:element[@name='rightsHolder']/doc:element/doc:element[@name='authority']">
            	<oai_cerif:Publishers>
             	<xsl:variable name="dc_rightholder_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='id']/text()" />
             	</xsl:variable>
           	    	<oai_cerif:Holder>
						<xsl:call-template name="person">
							<xsl:with-param name="person_id" select="$dc_rightholder_id" />
						</xsl:call-template>
					</oai_cerif:Holder>
           		</oai_cerif:Publishers>
            </xsl:for-each>
            <!-- oai_cerif:Holders [END] -->
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='description']/doc:element[@name='abstract']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Abstract><xsl:value-of select="." /></oai_cerif:Abstract>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='subject']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Subject><xsl:value-of select="." /></oai_cerif:Subject>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='subject']/doc:element[@name='keywords']/doc:element/doc:field[@name='value']">
	        	<oai_cerif:Keywords><xsl:value-of select="." /></oai_cerif:Keywords>
	        </xsl:for-each>
            
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element/doc:element[@name='authority']">
	       	<oai_cerif:OriginatesFrom>
	        	<xsl:variable name="dc_relation_id">
             		<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
             	</xsl:variable>
 					<xsl:call-template name="project">
						<xsl:with-param name="selector" select="." />
						<xsl:with-param name="dc_relation_id" select="$dc_relation_id" />
					</xsl:call-template>
 	        </oai_cerif:OriginatesFrom>
	        </xsl:for-each>
	        
	       	<!-- MISSING METADATA FIX [START] predecessor  -->
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='predecessor']/doc:element/doc:element[@name='authority']">
	       	<oai_cerif:Predecessor>
				<xsl:call-template name="patent">
					<xsl:with-param name="selector" select="." />
				</xsl:call-template>
	        </oai_cerif:Predecessor>
	        </xsl:for-each>
	        
	        <xsl:for-each select="doc:element[@name='dc']/doc:element[@name='relation']/doc:element[@name='isreferencedby']/doc:element/doc:element[@name='authority']">
	        	<oai_cerif:References>
             		<xsl:call-template name="item_chooser">
						<xsl:with-param name="selector" select="." />
					</xsl:call-template>
	        	</oai_cerif:References>
	        </xsl:for-each>
	        
        </oai_cerif:Patent>
        </xsl:for-each>
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
        	<!-- check type (is a publication or a product or ...) -->
        	<xsl:call-template name="item_chooser">
				<xsl:with-param name="selector" select="." />
			</xsl:call-template>
        </xsl:if>
        
        <!-- ou -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='ou'">
	        <xsl:variable name="parentorgunit_id">
	 			<xsl:value-of select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
		    </xsl:variable>
        	<xsl:call-template name="oai_parentorgunit">
		    	<xsl:with-param name="selector" select="doc:metadata" />
		        <xsl:with-param name="id" select="$parentorgunit_id" />
		        <xsl:with-param name="depth" select="1" />
		   	</xsl:call-template>
        </xsl:if>
        
        <!-- rp -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='rp'">
			<xsl:for-each select="doc:metadata">
				<xsl:variable name="rp_id">
		 			<xsl:value-of select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
			    </xsl:variable>
		 	               	
				<!-- Person call -->
				<xsl:call-template name="person">
					<xsl:with-param name="person_id" select="$rp_id" />
				</xsl:call-template>
			</xsl:for-each>
        </xsl:if>
        
        <!-- project -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='project'">
	        <xsl:variable name="dc_relation_id">
            	<xsl:value-of select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
            </xsl:variable>
			<xsl:call-template name="project">
				<xsl:with-param name="selector" select="doc:metadata" />
				<xsl:with-param name="dc_relation_id" select="$dc_relation_id" />
			</xsl:call-template>
        </xsl:if>
        
        <!-- event -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='events'">
	        <xsl:variable name="events_id">
            	<xsl:value-of select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
            </xsl:variable>
			<xsl:call-template name="events">
				<xsl:with-param name="selector" select="doc:metadata" />
				<xsl:with-param name="events_id" select="$events_id" />
			</xsl:call-template>
        </xsl:if>
        
        <!-- journals -->
        <xsl:if test="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='objecttype']/doc:element/doc:field[@name='value']/text()='journals'">
        	<xsl:variable name="journals_id">
            	<xsl:value-of select="doc:metadata/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']/text()" />
            </xsl:variable>
			<xsl:call-template name="publication_journal">
				<xsl:with-param name="selector" select="doc:metadata" />
				<xsl:with-param name="publication_id" select="$journals_id" />
			</xsl:call-template>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>