package com.docutools.users.resources

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request body for {@link com.docutools.users.PersonalApi#changePassword(ChangePasswordDTO)}
 */
class ChangePasswordDTO {

    private String oldPassword
    private String newPassword

    @JsonCreator
    ChangePasswordDTO(@JsonProperty(required = true, value = 'oldPassword') String oldPassword,
                      @JsonProperty(required = true, value = 'newPassword') String newPassword) {
        this.oldPassword = oldPassword
        this.newPassword = newPassword
    }

    String getOldPassword() {
        return oldPassword
    }

    String getNewPassword() {
        return newPassword
    }
}
