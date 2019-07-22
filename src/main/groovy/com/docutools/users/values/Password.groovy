package com.docutools.users.values

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import javax.persistence.Column
import java.time.LocalDateTime

/**
 * Represents a hashed password.
 */
@ApiModel(value = "Password Resource")
class Password {

    static final Pbkdf2_HASH_VERSION = "3526344901"

    @Column(name = 'password_hash')
    @ApiModelProperty(value = "The hash of the password")
    String hash
    @Column(name = 'password_last_changed')
    @ApiModelProperty(value = "When the password was last changed")
    LocalDateTime lastChanged
    @Column(name = 'password_hash_version')
    @ApiModelProperty(value = "The Hash version")
    String hashVersion

    def Password() {
    }

    Password(String hash) {
        this.hash = hash;
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Password password = (Password) o

        if (hash != password.hash) return false
        if (hashVersion != password.hashVersion) return false
        if (lastChanged != password.lastChanged) return false

        return true
    }

    int hashCode() {
        int result
        result = (hash != null ? hash.hashCode() : 0)
        result = 31 * result + (lastChanged != null ? lastChanged.hashCode() : 0)
        result = 31 * result + (hashVersion != null ? hashVersion.hashCode() : 0)
        return result
    }
}
