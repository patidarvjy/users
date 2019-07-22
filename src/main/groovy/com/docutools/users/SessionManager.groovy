package com.docutools.users


import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.stereotype.Service

import static com.docutools.exceptions.ExceptionHelper.*;

/**
 * Wraps around {@link SecurityContextHolder} and maps the current principal to {@link DocutoolsUser} instances.
 */
@Service
class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager)

    @Autowired
    private UserRepo userRepo
    @Autowired
    private OrganisationRepo organisationRepo

    /**
     * Tries to fetch the currently operatnig user from the spring security context and load the user details from
     * database.
     *
     * @return {@link DocutoolsUser} entity for the current user.
     * @throws com.docutools.apierrors.ApiException if the principal does not exist in the database or is {@code null}.
     */
    DocutoolsUser getCurrentUser() {
        def username = SecurityContextHolder.
                getContext()?.
                getAuthentication()?.
                getPrincipal()?.
                toString()
        userRepo.findByUsernameIgnoreCase(username)
            .orElseGet {
            try {
                organisationRepo.findById(UUID.fromString(username))
                        .orElseThrow {
                    log.warn('Could not retrieve user details from security context.')
                    throw newUnauthorizedError("Could not get user details")
                }.owner
            } catch (IllegalArgumentException e) {
                log.warn('Could not retrieve user details from security context.')
                throw newUnauthorizedError("Could not get user details")
            }
        }
    }

    static String getBearerToken() {
        // Get bearer token from security context
        def auth = SecurityContextHolder.getContext().getAuthentication()
        if (auth == null || !(auth instanceof OAuth2Authentication)) {
            return null
        }
        def oAuth2Auth = (OAuth2Authentication) auth
        def details = oAuth2Auth.getDetails();
        if (details == null || !(details instanceof OAuth2AuthenticationDetails)) {
            return null
        }

        def oAuth2Details = (OAuth2AuthenticationDetails) details
        def bearerToken = oAuth2Details.getTokenValue()
        if (bearerToken == null || bearerToken.isEmpty()) {
            return null
        }
        bearerToken
    }

}
