package com.docutools.users.resources

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 * Request body for {@link com.docutools.users.PersonalApi#updateEmailAddress(ChangeEmailDTO)}.
 */
@ApiModel(value = "Change Email Resource")
class ChangeEmailDTO {

    @ApiModelProperty(value = "Current Password")
    private String password
    @ApiModelProperty(value = "New Email to be set")
    private String newEmail

    @JsonCreator
    ChangeEmailDTO(@JsonProperty(required = true, value = 'password') String password,
                   @JsonProperty(required = true, value = 'newEmail') String newEmail) {
        this.password = password
        this.newEmail = newEmail
    }

    String getPassword() {
        return password
    }

    String getNewEmail() {
        return newEmail
    }
}
