package com.docutools.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/saml/token")
public class SAMLTokenEndpoint {

    private static final Logger log = LoggerFactory.getLogger(SAMLTokenEndpoint.class);

    @Autowired
    private SAMLTokenService tokenService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2AccessToken authorize(@RequestBody SAMLAuthenticationRequest authenticationRequest) {
        log.debug("POST /saml/token Body={}", authenticationRequest);
        return tokenService.authorize(authenticationRequest);
    }

}
