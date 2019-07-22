package com.docutools.oauth2;

import com.docutools.users.OrganisationRepo;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAuthority('sustain_user')")
@RequestMapping(path = "/client-credentials-api/v1")
public class ClientCredentialsController {

    private static final Logger log = LoggerFactory.getLogger(ClientCredentialsController.class);

    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private ClientCredentialsService service;

    @ApiOperation(value = "Renew Client Credentials", notes = "Creates new Client Credentials for the given Organisation.")
    @PutMapping(path = "/credentials")
    public HttpEntity<DocuToolsClientCredentials> renewCredentials(@RequestBody RenewCredentialsRequest request) {
        log.info("PUT /client-credentials-api-v1/credentials Body: {}", request);
        return service.createOrRenewCredentials(organisationRepo.findById(request.getOrganisation()).orElse(null))
                .map(credentials -> new ResponseEntity<>(credentials, HttpStatus.CREATED))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
