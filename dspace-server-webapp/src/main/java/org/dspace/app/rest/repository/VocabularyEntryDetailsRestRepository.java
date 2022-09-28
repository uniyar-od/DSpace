/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.DiscoverableEndpointsService;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.LinkNotFoundException;
import org.dspace.app.rest.exception.RepositoryMethodNotImplementedException;
import org.dspace.app.rest.model.ResourcePolicyRest;
import org.dspace.app.rest.model.VocabularyEntryDetailsRest;
import org.dspace.app.rest.model.VocabularyRest;
import org.dspace.app.rest.utils.AuthorityUtils;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.ChoiceAuthority;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.DSpaceControlledVocabulary;
import org.dspace.content.authority.ItemControlledVocabularyService;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.core.Context;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Controller for exposition of vocabularies entry details for the submission
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */
@Component(VocabularyRest.CATEGORY + "." + VocabularyEntryDetailsRest.NAME)
public class VocabularyEntryDetailsRestRepository extends DSpaceRestRepository<VocabularyEntryDetailsRest, String>
        implements InitializingBean {

    @Autowired
    private ChoiceAuthorityService cas;

    @Autowired
    private AuthorityUtils authorityUtils;

    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService.register(this, Arrays.asList(
                Link.of("/api/" + VocabularyRest.CATEGORY + "/" + VocabularyEntryDetailsRest.PLURAL_NAME + "/search",
                        VocabularyEntryDetailsRest.PLURAL_NAME + "-search")));
    }

    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    @Override
    public Page<VocabularyEntryDetailsRest> findAll(Context context, Pageable pageable) {
        throw new RepositoryMethodNotImplementedException(ResourcePolicyRest.NAME, "findAll");
    }

    @PreAuthorize("@vocabularySecurity.isQualifiedVocabularyPublic(#id) || hasAuthority('AUTHENTICATED')")
    @Override
    public VocabularyEntryDetailsRest findOne(Context context, String id) {
        String[] parts = StringUtils.split(id, ":", 2);
        if (parts.length != 2) {
            return null;
        }
        String vocabularyName = parts[0];
        String vocabularyId = parts[1];
        ChoiceAuthority source = cas.getChoiceAuthorityByAuthorityName(vocabularyName);
        Choice choice = source.getChoice(vocabularyId, context.getCurrentLocale().toString());
        //FIXME hack to deal with an improper use on the angular side of the node id (otherinformation.id) to
        // build a vocabulary entry details ID
        boolean fix = false;
        if (dspaceOrItemControlledVocabulary(source) && !StringUtils.startsWith(vocabularyId, vocabularyName)) {
            fix = true;
        }
        VocabularyEntryDetailsRest entryDetails = authorityUtils.convertEntryDetails(fix, choice, vocabularyName,
                source.isHierarchical(), utils.obtainProjection());
        //FIXME hack to deal with an improper use on the angular side of the node id (otherinformation.id) to
        // build a vocabulary entry details ID
        if (dspaceOrItemControlledVocabulary(source) && !StringUtils.startsWith(vocabularyId, vocabularyName)
                && entryDetails != null) {
            entryDetails.setId(id);
        }
        return entryDetails;
    }

    @SearchRestMethod(name = "top")
    @PreAuthorize("@vocabularySecurity.isVocabularyPublic(#vocabularyId) || hasAuthority('AUTHENTICATED')")
    public Page<VocabularyEntryDetailsRest> findAllTop(@Parameter(value = "vocabulary", required = true)
           String vocabularyId, Pageable pageable) {
        Context context = obtainContext();
        List<VocabularyEntryDetailsRest> results = new ArrayList<VocabularyEntryDetailsRest>();
        ChoiceAuthority source = cas.getChoiceAuthorityByAuthorityName(vocabularyId);
        if (source.isHierarchical()) {
            Choices choices = cas.getTopChoices(vocabularyId, (int)pageable.getOffset(), pageable.getPageSize(),
                    context.getCurrentLocale().toString());
            //FIXME hack to deal with an improper use on the angular side of the node id (otherinformation.id) to
            // build a vocabulary entry details ID
            boolean fix = false;
            if (dspaceOrItemControlledVocabulary(source)) {
                //FIXME this will stop to work once angular starts to deal with the proper form of the entry id...
                fix = true;
            }
            for (Choice value : choices.values) {
                results.add(authorityUtils.convertEntryDetails(fix, value, vocabularyId, source.isHierarchical(),
                        utils.obtainProjection()));
            }
            Page<VocabularyEntryDetailsRest> resources = new PageImpl<VocabularyEntryDetailsRest>(results, pageable,
                    choices.total);
            return resources;
        }
        throw new LinkNotFoundException(VocabularyRest.CATEGORY, VocabularyEntryDetailsRest.NAME, vocabularyId);
    }

    private boolean dspaceOrItemControlledVocabulary(ChoiceAuthority source) {
        return source instanceof DSpaceControlledVocabulary || source instanceof ItemControlledVocabularyService;
    }

    @Override
    public Class<VocabularyEntryDetailsRest> getDomainClass() {
        return VocabularyEntryDetailsRest.class;
    }
}
