package com.docutools.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class DocuToolsClientCredentials implements Serializable {

    private String clientId;
    private String clientSecret;

    @JsonCreator
    public DocuToolsClientCredentials(@JsonProperty("clientId") String clientId, @JsonProperty("clientSecret") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
