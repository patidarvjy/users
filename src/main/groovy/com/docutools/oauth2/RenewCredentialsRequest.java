package com.docutools.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.UUID;

@ApiModel(description = "Request for renewing a customers API Client Credentials.")
public class RenewCredentialsRequest implements Serializable {

    @ApiModelProperty("ID of the organisation.")
    private UUID organisation;

    @JsonCreator
    public RenewCredentialsRequest(@JsonProperty(value = "organisation", required = true) UUID organisation) {
        this.organisation = organisation;
    }

    public UUID getOrganisation() {
        return organisation;
    }

    @Override
    public String toString() {
        return "RenewCredentialsRequest{" +
                "organisation=" + organisation +
                '}';
    }
}
