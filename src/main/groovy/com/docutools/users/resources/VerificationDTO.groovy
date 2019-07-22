package com.docutools.users.resources

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request body when a user verified an account.
 */
class VerificationDTO {

    @JsonProperty(required = true)
    String token
    @JsonProperty(required = true)
    String password

}
