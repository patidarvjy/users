package com.docutools.users.values

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import javax.persistence.Column
import javax.persistence.Embeddable
import java.time.LocalDateTime

/**
 * Represents the state of a users verification process.
 */
@Embeddable
@ApiModel(value = "Verification Status Resource")
class VerificationStatus {

    @Column(nullable = false)
    @ApiModelProperty(value = "Whether the verification is required or not")
    boolean verificationRequired = true
    @Column(nullable = false)
    @ApiModelProperty(value = "Whether it is verified or not")
    boolean verified = false
    @Column(name = 'verification_token', nullable = false)
    @ApiModelProperty(value = "The verification token")
    String token
    @Column(name = 'verification_token_expiry_time', nullable = false)
    @ApiModelProperty(value = "The expiry time of the token")
    LocalDateTime expiryTime
    @Column(name = 'verification_password_reset')
    @ApiModelProperty(value = "Whether a password reset has occurred or not")
    boolean passwordReset = false

    def VerificationStatus() {
        token = UUID.randomUUID().toString()
        expiryTime = LocalDateTime.now().plusDays(7)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        VerificationStatus that = (VerificationStatus) o

        if (verificationRequired != that.verificationRequired) return false
        if (expiryTime != that.expiryTime) return false
        if (token != that.token) return false

        return true
    }

    int hashCode() {
        int result
        result = (verificationRequired ? 1 : 0)
        result = 31 * result + (token != null ? token.hashCode() : 0)
        result = 31 * result + (expiryTime != null ? expiryTime.hashCode() : 0)
        return result
    }
}
