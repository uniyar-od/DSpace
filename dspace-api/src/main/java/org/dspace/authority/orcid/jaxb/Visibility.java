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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for visibility.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="visibility">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="private"/>
 *     &lt;enumeration value="limited"/>
 *     &lt;enumeration value="public"/>
 *     &lt;enumeration value="registered-only"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "visibility")
@XmlEnum
public enum Visibility {


    /**
     * The data can only be seen by the researcher or contributor. This data may be used internally by ORCID for Record disambiguation purposes.
     * 					
     * 
     */
    @XmlEnumValue("private")
    PRIVATE("private"),

    /**
     * The data can only be seen by trusted parties (organizations or people) as indicated by the researcher or contributor. This information is only shared with systems that the researcher or contributor has specifically granted authorization (using OAuth).
     * 					
     * 
     */
    @XmlEnumValue("limited")
    LIMITED("limited"),

    /**
     * The data can be seen by anyone. It is publically available via the ORCID Registry website and the public API without further authroization by the researcher or contributor.
     * 					
     * 
     */
    @XmlEnumValue("public")
    PUBLIC("public"),

    /**
     * The data is shared only with the registered user.
     * 					
     * 
     */
    @XmlEnumValue("registered-only")
    REGISTERED_ONLY("registered-only");
    private final String value;

    Visibility(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Visibility fromValue(String v) {
        for (Visibility c: Visibility.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
