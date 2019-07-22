package com.docutools.saml;

import com.docutools.config.security.DocutoolsUserDetails;
import com.docutools.exceptions.ErrorCodes;
import com.docutools.exceptions.ExceptionHelper;
import com.docutools.login.LoginTracker;
import com.docutools.users.*;
import com.docutools.users.resources.SettingsDTO;
import com.docutools.users.resources.UserDTO;
import com.docutools.users.values.PersonName;
import com.docutools.users.values.UserSettings;
import com.docutools.users.values.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;

import static com.docutools.exceptions.ExceptionHelper.newResourceNotFoundError;
import static com.docutools.exceptions.ExceptionHelper.newUnauthorizedError;

@Service
public class SAMLTokenService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private SAMLRequestVerifier samlRequestVerifier;
    @Autowired
    private AuthorizationServerEndpointsConfiguration configuration;
    @Autowired
    private UserManager userManager;
    @Autowired
    private LoginTracker loginTracker;
    @Autowired
    private RegistrationService registrationService;

    public OAuth2AccessToken authorize(SAMLAuthenticationRequest authenticationRequest) {
        Assert.notNull(authenticationRequest, "authenticationRequest is required - must not be NULL");

        if(!samlRequestVerifier.verify(authenticationRequest)){
            throw newUnauthorizedError("Invalid Signature in SAMLAuthenticationRequest.");
        }

        DocutoolsUser user = refreshUser(userRepo.findByUsernameIgnoreCase(authenticationRequest.getUsername())
                .orElseGet(() -> this.createNewSAMLUser(authenticationRequest)), authenticationRequest);


        if (user.getType() != UserType.SAML) {
            if(user.isOrganisationOwner() && user.getOrganisation().getSubscription().isActive()) {
                throw newUnauthorizedError("This is not a SAML User.");
            }
            // Change user to saml from normal if the user already has an user
            registrationService.migrateUserToSAML(user, authenticationRequest.getIdp());
        }

        if (!authenticationRequest.sameIdPAs(user.getOrganisation())) {
            throw newUnauthorizedError("IdP Link miss match");
        }

        loginTracker.trackLogin(user);

        return generateOAuth2AccessToken(user);
    }

    /**
     * Generates a OAuth 2 Access Token packed in a JWT for the given {@link DocutoolsUser}.
     *
     * @param user the {@link DocutoolsUser}
     * @return a new {@link OAuth2AccessToken} for the given {@link DocutoolsUser}
     */
    private OAuth2AccessToken generateOAuth2AccessToken(DocutoolsUser user) {

        Map<String, String> requestParameters = new HashMap<>();
        Map<String, Serializable> extensionProperties = new HashMap<>();

        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");

        // Authorities
        List<GrantedAuthority> grantedAuthorities = DocutoolsUserDetails.buildAuthorities(user);

        HashSet<String> scope = new HashSet<>(Arrays.asList("users", "organisations"));

        OAuth2Request oauth2Request = new OAuth2Request(requestParameters, "tester", grantedAuthorities,
                true, scope, null,
                null, responseTypes, extensionProperties);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), "N/A", grantedAuthorities);
        authenticationToken.setDetails(createDetails(user));

        OAuth2Authentication auth = new OAuth2Authentication(oauth2Request, authenticationToken);

        AuthorizationServerTokenServices tokenService = configuration.getEndpointsConfigurer().getTokenServices();

        return tokenService.createAccessToken(auth);
    }

    private Object createDetails(DocutoolsUser user) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("username", user.getUsername());
        return details;
    }

    private DocutoolsUser refreshUser(DocutoolsUser user, SAMLAuthenticationRequest authenticationRequest) {
        PersonName name = new PersonName();
        name.setFirstName(authenticationRequest.getFirstName());
        name.setLastName(authenticationRequest.getLastName());
        user.setName(name);

        if(!StringUtils.isEmpty(authenticationRequest.getEmail())) {
            user.setEmail(authenticationRequest.getEmail());
        }

        String samlLanguage = authenticationRequest.getLanguage();
        if(!StringUtils.isEmpty(samlLanguage) && samlLanguage.length() >= 2) {
            samlLanguage = samlLanguage.substring(0, 2);
            if(samlLanguage.matches("[a-zA-Z]+")) {
                UserSettings settings = user.getSettings();
                settings.setLanguage(samlLanguage);
            }
        }

        // For users who belong to the Boehringer Ingelheim IdP, we don't update the language und re-login.
        // This is due to a bug in their SAML Service.
        if(!isBI(user)) {
            String samlCompany = authenticationRequest.getCompany();
            if (!StringUtils.isEmpty(samlCompany)) {
                user.getOrganisation().getNames()
                        .stream()
                        .filter(n -> n.getName().equals(samlCompany))
                        .findAny()
                        .ifPresent(user::setOrganisationName);
            }
        }

        return userRepo.save(user);
    }

    private DocutoolsUser createNewSAMLUser(SAMLAuthenticationRequest authenticationRequest) {

        if (StringUtils.isEmpty(authenticationRequest.getIdp())) {
            throw ExceptionHelper.newBadRequestError(ErrorCodes.MISSING_REQUIRED_VALUE, "Idp link");
        }
        Organisation organisation = organisationRepo.findByIdpLink(authenticationRequest.getIdp()).
            orElseThrow(() -> newResourceNotFoundError("organisation with idlLink"));

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(authenticationRequest.getUsername());
        userDTO.setEmail(authenticationRequest.getEmail());

        String samlLanguage = authenticationRequest.getLanguage();
        if(!StringUtils.isEmpty(samlLanguage) && samlLanguage.length() >= 2) {
            samlLanguage = samlLanguage.substring(0, 2);
            if(samlLanguage.matches("[a-zA-Z]+")) {
                SettingsDTO settings = new SettingsDTO();
                settings.setLanguage(samlLanguage);
                userDTO.setSettings(settings);
            }
        }

        userDTO.setType(UserType.SAML);
        return userManager.inviteNewUser(userDTO, organisation);
    }

    private boolean isBI(DocutoolsUser user) {
        return user != null && user.getUsername() != null && user.getOrganisation() != null &&
                user.getOrganisation().getIdpLink() != null &&
                        user.getOrganisation().getIdpLink().compareToIgnoreCase("pfp1.boehringer.com") == 0;
    }

    private void changeUserToSAML(SAMLAuthenticationRequest authenticationRequest, DocutoolsUser user){
        user.setType(UserType.SAML);

    }
}
