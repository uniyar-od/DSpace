<?xml version="1.0" encoding="UTF-8" ?>
<!--
 
 
 The contents of this file are subject to the license and copyright
 detailed in the LICENSE and NOTICE files at the root of the source
 tree and available online at
 
 http://www.dspace.org/license/
 Developed by DSpace @ Lyncode <dspace@lyncode.com>
 
 > http://www.openarchives.org/OAI/2.0/oai_dc.xsd
 
 -->
<xsl:stylesheet
	xmlns:oai_cerif="https://www.openaire.eu/cerif-profile/1.1/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:doc="http://www.lyncode.com/xoai"
    version="1.0">
    <xsl:output omit-xml-declaration="yes" method="xml" indent="yes" />
    
    <xsl:key name="dc_relation_ispartof" match="doc:metadata/doc:element[@name='dc.relation.ispartof']" use="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element/doc:element/doc:field[@name = 'id']" />
    <xsl:key name="dc_contributor_author" match="doc:metadata/doc:element[@name='dc.contributor.author']" use="//doc:field[@name='id']" />
    <xsl:key name="affiliation.affiliationorgunit" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="crisou_parentorgunit__depth1" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
	<xsl:key name="crisou_parentorgunit__depth2" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="crisou_parentorgunit__depth3" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    <xsl:key name="crisou_parentorgunit__depth4" match="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']" use="//doc:field[@name='id']" />
    		
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
    
    <!--	
    	oai_parentorgunit: Ricorsive template that handle orgunit.
    	
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
				<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
			
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
						            <xsl:with-param name="key" select="'crisou_parentorgunit__depth'" />
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

    
    <xsl:template match="/">
        <!-- NEW: virtuals medatata
         crisitem.crisvprop.id        	(the id of the cris item)
         crisitem.crisvprop.uuid      	(the uuid of the cris item)
         crisitem.crisvprop.handle    	(the handle of the cris item)
         crisitem.crisvprop.fullname	(the full name, supports two formats: familyname, firstname or firstname familyname)
         crisitem.crisvprop.familyname	(the family name)
         crisitem.crisvprop.firstname	(the first name)
         
         crisitem.crisvprop.title       (the title of the journal)
         crisitem.crisvprop.issn        (the issn of the journal)
         
         item.vprop.type				(usually dc.type)
         item.vprop.title				(usually dc.title)
         item.vrelation.ispartof		(usually dc.relation.ispartof)
         -->
        <xsl:variable name="item_prop_id">
            <xsl:value-of select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='id']/doc:element/doc:field[@name='value']" />
        </xsl:variable>
        
        <oai_cerif:Publication xmlns:oai_cerif="https://www.openaire.eu/cerif-profile/1.1/"
            xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd https://www.openaire.eu/cerif-profile/1.1/ https://www.openaire.eu/schema/cris/1.1/openaire-cerif-profile.xsd"
            id="{$item_prop_id}">
            
            <!-- dc.type BUG: one /doc:element removed -->
            <xsl:variable name="dc_type_ci">
                <xsl:value-of select="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']" />
            </xsl:variable>
            <xsl:variable name="dc_type" select="translate($dc_type_ci,'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
            
            <oai_cerif:Type xmlns="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
            	<xsl:call-template name="oai_type"><xsl:with-param name="type" select="$dc_type" /></xsl:call-template>
            </oai_cerif:Type>
            
            <!-- dc.title (BUG, missing one /doc:element)-->
            <xsl:for-each select="doc:metadata/doc:element[@name='dc']/doc:element[@name='title']/doc:element/doc:field[@name='value']">
                <oai_cerif:Title><xsl:value-of select="." /></oai_cerif:Title>
            </xsl:for-each>
            
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
                        <oai_cerif:Type xmlns="https://www.openaire.eu/cerif-profile/vocab/COAR_Publication_Types">
                            <xsl:call-template name="oai_type"><xsl:with-param name="type" select="$crisitem_crisvprop_type" /></xsl:call-template>
                        </oai_cerif:Type>
                    
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
            
            <!--  MISSING METADATA FIX [START]: volume, issue, startpage, endpage -->
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='volume']/doc:element/doc:field[@name='value']">
                <oai_cerif:Volume><xsl:value-of select="." /></oai_cerif:Volume>
            </xsl:for-each>
            
            <xsl:for-each select="doc:metadata/doc:element[@name='item']/doc:element[@name='vprop']/doc:element[@name='issue']/doc:element/doc:field[@name='value']">
                <oai_cerif:Issue><xsl:value-of select="." /></oai_cerif:Issue>
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
            
            <!-- oai_cerif:Authors [START] -->
            <oai_cerif:Authors>
            <xsl:for-each select="doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
            
             	<xsl:variable name="dc_contributor_author_id">
             		<xsl:value-of select="doc:field[@name='id']/text()" />
             	</xsl:variable>
            	<xsl:for-each select="key('dc_contributor_author', doc:field[@name='id']/text())">
            		<oai_cerif:Author>
                        <!-- oai_cerif:Authors, DisplayName -->
 	            		<xsl:for-each select="doc:element[@name='crisrp']/doc:element[@name='fullname']/doc:element">
 	            			<xsl:if test="doc:field[@name='id']=$dc_contributor_author_id">
	                   		<oai_cerif:DisplayName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:DisplayName>
	                   		</xsl:if>
	                    </xsl:for-each>

						<xsl:variable name="author_crisitem_crisprop_id">
						<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
  							<xsl:if test="doc:field[@name='id']=$dc_contributor_author_id">
  								<xsl:value-of select="doc:field[@name='value']" />
		                	</xsl:if>
		      			</xsl:for-each>
		      			</xsl:variable>
		      			
		      			<xsl:variable name="author_crisitem_crisprop_uuid">
						<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='uuid']/doc:element">
  							<xsl:if test="doc:field[@name='id']=$dc_contributor_author_id">
  								<xsl:value-of select="doc:field[@name='value']" />
		                	</xsl:if>
		      			</xsl:for-each>
		      			</xsl:variable>
 	 	               	
 	 	               	<!-- oai_cerif:Person [START] -->
		                <oai_cerif:Person id="{$author_crisitem_crisprop_id}"> 
		                	<oai_cerif:PersonName>
		                		<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='familyname']/doc:element">
		                		<xsl:if test="doc:field[@name='id']=$dc_contributor_author_id">
                					<oai_cerif:FamilyName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:FamilyName>
                				</xsl:if>
            					</xsl:for-each>
            					
            					<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='firstname']/doc:element">
                				<xsl:if test="doc:field[@name='id']=$dc_contributor_author_id">
                					<oai_cerif:PersonName><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:PersonName>
                				</xsl:if>
            					</xsl:for-each>
		                	</oai_cerif:PersonName>
		                    
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
			 				<xsl:for-each select="key('affiliation.affiliationorgunit', doc:field[@name='id']/text())">
							<!-- only value with relation id equals to author uuid -->
			                <oai_cerif:Affiliation>
			                	<!-- only value with relation equal to author uuid will be processed -->
								<xsl:call-template name="oai_parentorgunit">
									<xsl:with-param name="selector" select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element" />
									<xsl:with-param name="id" select="$affiliation.affiliationorgunit_id" />
									<xsl:with-param name="uuid" select="$affiliation.affiliationorgunit_uuid" />
									<xsl:with-param name="relid" select="$affiliation.affiliationorgunit_relid" />
									<xsl:with-param name="key" select="'crisou_parentorgunit__depth'" />
									<xsl:with-param name="depth" select="1" />
								</xsl:call-template>
			                		
			                	<!-- TODO: Remove (code replaced by template oai_parentorgunit)
			                	<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element">
			                	
			                		<xsl:if test="doc:field[@name='relid']/text()=$affiliation.affiliationorgunit_relid and doc:field[@name='id']/text()=$affiliation.affiliationorgunit_uuid">
	                				<oai_cerif:OrgUnit id="{$affiliation.affiliationorgunit_id}">
	                					<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
	                					
	                					// goto, crisou.parentorgunit depth 1
										<xsl:for-each select="/doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
										<xsl:variable name="crisou_parentorgunit_id__depth1">
	            							<xsl:value-of select="doc:field[@name='value']/text()" />
				             			</xsl:variable>
				             			<xsl:variable name="crisou_parentorgunit_uuid__depth1">
				             				<xsl:value-of select="doc:field[@name='id']/text()" />
				             			</xsl:variable>
				             			<xsl:variable name="crisou_parentorgunit_relid__depth1">
				             				<xsl:value-of select="doc:field[@name='relid']/text()" />
				             			</xsl:variable>
				             			<xsl:if test="$crisou_parentorgunit_relid__depth1=$affiliation.affiliationorgunit_uuid">
										<xsl:for-each select="key('crisou_parentorgunit__depth1', doc:field[@name='id']/text())">
											// grab next id
											<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
												<xsl:if test="doc:field[@name='relid']/text()=$crisou_parentorgunit_relid__depth1 and doc:field[@name='id']/text()=$crisou_parentorgunit_uuid__depth1">
													<xsl:variable name="item_relation_parentorgunit_id__depth1">
		            								<xsl:value-of select="doc:field[@name='value']/text()" />
					             					</xsl:variable>
					             					<xsl:variable name="item_relation_parentorgunit_uuid__depth1">
					             					<xsl:value-of select="doc:field[@name='id']/text()" />
					             					</xsl:variable>
					             					<xsl:variable name="item_relation_parentorgunit_relid__depth1">
					             					<xsl:value-of select="doc:field[@name='relid']/text()" />
					             					</xsl:variable>
					             					// [DEBUG-id]<xsl:value-of select="."></xsl:value-of>
												</xsl:if>
											</xsl:for-each>
											
											// grab name
											<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element">
											<xsl:if test="doc:field[@name='relid']/text()=$crisou_parentorgunit_relid__depth1 and doc:field[@name='id']/text()=$crisou_parentorgunit_uuid__depth1">
											<oai_cerif:PartOf>
												// [DEBUG-name]<xsl:value-of select="."></xsl:value-of>
	                							<oai_cerif:OrgUnit id="{$crisou_parentorgunit_id__depth1}">
	                								<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
	                								
	                								// goto, crisou.parentorgunit depth 2
	                								// xsl:for-each select="/doc:metadata/doc:element[@name='dc.contributor.author']/doc:element[@name='affiliation.affiliationorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element"
													<xsl:for-each select="../../../doc:element[@name='crisou.parentorgunit']/doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
													<xsl:variable name="crisou_parentorgunit_id__depth2">
				            							<xsl:value-of select="doc:field[@name='value']/text()" />
							             			</xsl:variable>
							             			<xsl:variable name="crisou_parentorgunit_uuid__depth2">
							             				<xsl:value-of select="doc:field[@name='id']/text()" />
							             			</xsl:variable>
							             			<xsl:variable name="crisou_parentorgunit_relid__depth2">
							             				<xsl:value-of select="doc:field[@name='relid']/text()" />
							             			</xsl:variable>
							             			<xsl:if test="$crisou_parentorgunit_relid__depth2=$crisou_parentorgunit_uuid__depth1">
							             			// [DEBUG__depth2]<xsl:value-of select="."></xsl:value-of>
							             			<xsl:for-each select="key('crisou_parentorgunit__depth2', doc:field[@name='id']/text())">
							             				// grab next id 
														<xsl:for-each select="doc:element[@name='crisitem']/doc:element[@name='crisvprop']/doc:element[@name='id']/doc:element">
															<xsl:if test="doc:field[@name='relid']/text()=$crisou_parentorgunit_relid__depth2 and doc:field[@name='id']/text()=$crisou_parentorgunit_uuid__depth2">
																<xsl:variable name="item_relation_parentorgunit_id__depth2">
					            								<xsl:value-of select="doc:field[@name='value']/text()" />
								             					</xsl:variable>
								             					<xsl:variable name="item_relation_parentorgunit_uuid__depth2">
								             					<xsl:value-of select="doc:field[@name='id']/text()" />
								             					</xsl:variable>
								             					<xsl:variable name="item_relation_parentorgunit_relid__depth2">
								             					<xsl:value-of select="doc:field[@name='relid']/text()" />
								             					</xsl:variable>
								             					// [DEBUG-id]<xsl:value-of select="."></xsl:value-of>
															</xsl:if>
														</xsl:for-each>
														
														// grab name
														<xsl:for-each select="doc:element[@name='crisou']/doc:element[@name='name']/doc:element">
														<xsl:if test="doc:field[@name='relid']/text()=$crisou_parentorgunit_relid__depth2 and doc:field[@name='id']/text()=$crisou_parentorgunit_uuid__depth2">
														<oai_cerif:PartOf>
									             			// [DEBUG-name]<xsl:value-of select="."></xsl:value-of>
			                								<oai_cerif:OrgUnit id="{$crisou_parentorgunit_id__depth2}">
			                									<oai_cerif:Name><xsl:value-of select="doc:field[@name='value']" /></oai_cerif:Name>
			                									
			                									// goto, crisou.parentorgunit depth 3
			                								</oai_cerif:OrgUnit>
			                							</oai_cerif:PartOf>
			                							</xsl:if>
			                							</xsl:for-each>
							             			</xsl:for-each>
							             			</xsl:if>
							             			</xsl:for-each>
				             			
	                							</oai_cerif:OrgUnit>
	                						</oai_cerif:PartOf>
	                						</xsl:if>
	                						</xsl:for-each>
	             			 			</xsl:for-each>
	             			 			</xsl:if>
	             			 			</xsl:for-each>
	             			 			
	                				</oai_cerif:OrgUnit>
	                				</xsl:if>
	            					
	            				</xsl:for-each>
	            				-->
	            			</oai_cerif:Affiliation>
		                	</xsl:for-each>
            				</xsl:if>
            					                
		      				</xsl:for-each>
		                	<!-- oai_cerif:Affiliation [END] -->
		             	</oai_cerif:Person>
		                <!-- oai_cerif:Person [END] -->
		                
                    </oai_cerif:Author>
				</xsl:for-each>

            </xsl:for-each>
           	</oai_cerif:Authors>
            <!-- oai_cerif:Authors [END] -->
            
        </oai_cerif:Publication>
    </xsl:template>

</xsl:stylesheet>