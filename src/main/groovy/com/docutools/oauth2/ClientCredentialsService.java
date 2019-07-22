package com.docutools.oauth2;

import com.docutools.config.security.PasswordEncoder;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class ClientCredentialsService {

    private static final Logger log = LoggerFactory.getLogger(ClientCredentialsService.class);

    @Autowired
    private OAuth2ClientCredentialsRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrganisationRepo organisationRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('sustain_user')")
    public boolean hasCredentials(Organisation organisation) {
        if(organisation == null || organisation.getId() == null) {
            log.warn("hasCredentials(Organisation) returns false, since organisation NULL or ID is NULL.");
            return false;
        }

        String clientId = organisation.getId().toString();
        return repository.existsById(clientId);
    }

    @Transactional
    @PreAuthorize("hasAuthority('sustain_user')")
    public Optional<DocuToolsClientCredentials> createOrRenewCredentials(Organisation organisation) {
        if(organisation == null || organisation.getId() == null) {
            log.warn("createOrRenewCredentials(Organisation) returns empty, since organisation NULL or ID is NULL.");
            return Optional.empty();
        }

        String clientId = organisation.getId().toString();
        String newSecret = UUID.randomUUID().toString();
        String encodedSecret = passwordEncoder.hashPassword(newSecret).getHash();

        OAuth2ClientCredentials clientCredentials = repository.findById(clientId)
                .map(credentials -> credentials.renewSecret(encodedSecret))
                .orElseGet(() -> OAuth2ClientCredentials.forOrganisation(organisation, encodedSecret));
        repository.save(clientCredentials);

        return Optional.of(new DocuToolsClientCredentials(clientId, newSecret));
    }

}
