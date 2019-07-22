package com.docutools.users.resources

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a anonymous users registration request.
 */
class RegistrationDTO {

    @JsonProperty(required = true)
    String email
    String firstName
    String lastName
    @JsonProperty(required = true)
    String organisationName
    @JsonProperty(required = true)
    String countryCode
    String language
    String phone

}
