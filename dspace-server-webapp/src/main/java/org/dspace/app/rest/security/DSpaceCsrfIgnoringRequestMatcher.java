/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class DSpaceCsrfIgnoringRequestMatcher implements RequestMatcher {

    private CsrfTokenRepository csrfTokenRepository;

    public DSpaceCsrfIgnoringRequestMatcher(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return hasNotCsrfCookie(request);
    }

    private boolean hasNotCsrfCookie(HttpServletRequest request) {
        return csrfTokenRepository.loadToken(request) == null;
    }

}
