package com.docutools.oauth2;

import com.docutools.users.Organisation;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "oauth_client_details")
public class OAuth2ClientCredentials {

    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final long DEFAULT_ACCESS_TOKEN_VALIDITY = 86400;
    private static final String DEFAULT_SCOPE = "users,organisations";

    public static OAuth2ClientCredentials forOrganisation(Organisation organisation, String clientSecret) {
        return new OAuth2ClientCredentials(organisation.getId().toString(),
                clientSecret,
                CLIENT_CREDENTIALS_GRANT_TYPE,
                DEFAULT_ACCESS_TOKEN_VALIDITY,
                DEFAULT_SCOPE);
    }

    @Id
    private String clientId;
    private String clientSecret;
    private String authorizedGrantTypes;
    private Long accessTokenValidity;
    private String scope;

    @PersistenceConstructor
    protected OAuth2ClientCredentials() {
    }

    private OAuth2ClientCredentials(String clientId,
                                    String clientSecret,
                                    String authorizedGrantTypes,
                                    long accessTokenValidity,
                                    String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authorizedGrantTypes = authorizedGrantTypes;
        this.accessTokenValidity = accessTokenValidity;
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public OAuth2ClientCredentials renewSecret(String newSecret) {
        this.clientSecret = newSecret;
        return this;
    }


}
