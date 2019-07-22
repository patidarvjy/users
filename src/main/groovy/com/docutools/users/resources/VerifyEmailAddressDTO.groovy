package com.docutools.users.resources

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request body for {@link com.docutools.users.PersonalApi#verifyNewEmailAddress(VerifyEmailAddressDTO)}.
 */
class VerifyEmailAddressDTO {

    private String token

    @JsonCreator
    VerifyEmailAddressDTO(@JsonProperty(required = false, value = 'token') String token) {
        this.token = token
    }

    String getToken() {
        return token
    }
}
