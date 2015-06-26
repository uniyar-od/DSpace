/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.16 at 06:33:14 PM CEST 
//


package org.dspace.authority.orcid.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}send-email-frequency-days"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}send-change-notifications"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}send-orcid-news"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}send-member-update-requests"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}activities-visibility-default"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}developer-tools-enabled" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sendEmailFrequencyDays",
    "sendChangeNotifications",
    "sendOrcidNews",
    "sendMemberUpdateRequests",
    "activitiesVisibilityDefault",
    "developerToolsEnabled"
})
@XmlRootElement(name = "preferences")
public class Preferences {

    @XmlElement(name = "send-email-frequency-days", required = true)
    protected String sendEmailFrequencyDays;
    @XmlElement(name = "send-change-notifications", required = true)
    protected SendChangeNotifications sendChangeNotifications;
    @XmlElement(name = "send-orcid-news", required = true)
    protected SendOrcidNews sendOrcidNews;
    @XmlElement(name = "send-member-update-requests")
    protected boolean sendMemberUpdateRequests;
    @XmlElement(name = "activities-visibility-default", required = true)
    protected ActivitiesVisibilityDefault activitiesVisibilityDefault;
    @XmlElement(name = "developer-tools-enabled")
    protected DeveloperToolsEnabled developerToolsEnabled;

    /**
     * Gets the value of the sendEmailFrequencyDays property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSendEmailFrequencyDays() {
        return sendEmailFrequencyDays;
    }

    /**
     * Sets the value of the sendEmailFrequencyDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSendEmailFrequencyDays(String value) {
        this.sendEmailFrequencyDays = value;
    }

    /**
     * Gets the value of the sendChangeNotifications property.
     * 
     * @return
     *     possible object is
     *     {@link SendChangeNotifications }
     *     
     */
    public SendChangeNotifications getSendChangeNotifications() {
        return sendChangeNotifications;
    }

    /**
     * Sets the value of the sendChangeNotifications property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendChangeNotifications }
     *     
     */
    public void setSendChangeNotifications(SendChangeNotifications value) {
        this.sendChangeNotifications = value;
    }

    /**
     * Gets the value of the sendOrcidNews property.
     * 
     * @return
     *     possible object is
     *     {@link SendOrcidNews }
     *     
     */
    public SendOrcidNews getSendOrcidNews() {
        return sendOrcidNews;
    }

    /**
     * Sets the value of the sendOrcidNews property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendOrcidNews }
     *     
     */
    public void setSendOrcidNews(SendOrcidNews value) {
        this.sendOrcidNews = value;
    }

    /**
     * Gets the value of the sendMemberUpdateRequests property.
     * 
     */
    public boolean isSendMemberUpdateRequests() {
        return sendMemberUpdateRequests;
    }

    /**
     * Sets the value of the sendMemberUpdateRequests property.
     * 
     */
    public void setSendMemberUpdateRequests(boolean value) {
        this.sendMemberUpdateRequests = value;
    }

    /**
     * Gets the value of the activitiesVisibilityDefault property.
     * 
     * @return
     *     possible object is
     *     {@link ActivitiesVisibilityDefault }
     *     
     */
    public ActivitiesVisibilityDefault getActivitiesVisibilityDefault() {
        return activitiesVisibilityDefault;
    }

    /**
     * Sets the value of the activitiesVisibilityDefault property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivitiesVisibilityDefault }
     *     
     */
    public void setActivitiesVisibilityDefault(ActivitiesVisibilityDefault value) {
        this.activitiesVisibilityDefault = value;
    }

    /**
     * Gets the value of the developerToolsEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link DeveloperToolsEnabled }
     *     
     */
    public DeveloperToolsEnabled getDeveloperToolsEnabled() {
        return developerToolsEnabled;
    }

    /**
     * Sets the value of the developerToolsEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeveloperToolsEnabled }
     *     
     */
    public void setDeveloperToolsEnabled(DeveloperToolsEnabled value) {
        this.developerToolsEnabled = value;
    }

}
