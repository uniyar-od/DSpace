/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Methods of this class are used on PreAuthorize annotations
 * to check security on vocabulary endpoint
 * 
 * @author Davide Negretti (davide.negretti at 4science.it)
 */
@Component(value = "vocabularySecurity")
public class VocabularySecurityBean {

    @Autowired
    private ChoiceAuthorityService choiceAuthorityService;

    /**
     * This method checks if a vocabulary is public.
     *
     * @param authorityName is the authority name used for the vocabulary
     * @return the value of `authority.AUTHORITY_NAME.public`, or false if not set
     */
    public boolean isVocabularyPublic(String authorityName) {
        ChoiceAuthority choice = choiceAuthorityService.getChoiceAuthorityByAuthorityName(authorityName);
        return choice.isPublic();
    }

    /**
     * This method checks if a qualified vocabulary identifier is public.
     *
     * @param vocabularyQualifier is composed by two parts `{authorityName}:{identifier}`,
     * and identifies a single vocabulary entry.
     * @return the value of `authority.AUTHORITY_NAME.public`, or false if not set
     */
    public boolean isQualifiedVocabularyPublic(String vocabularyQualifier) {
        if (vocabularyQualifier == null) {
            return false;
        }
        String[] splittedQualifier = vocabularyQualifier.split(":");
        if (splittedQualifier.length < 2) {
            return false;
        }
        return this.isVocabularyPublic(splittedQualifier[0]);
    }

}
