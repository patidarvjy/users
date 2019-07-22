package com.docutools.users.values

import com.docutools.users.DocutoolsUser
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table
import java.time.LocalDateTime

import static javax.persistence.GenerationType.IDENTITY

/**
 * An {@link DocutoolsUser}s request to change the accounts email address.
 */
@Entity
@Table(name = 'change_email_requests')
class ChangeEmailRequest {

    @Id
    @Type(type = "pg-uuid")
    UUID id = UUID.randomUUID()
    @ManyToOne(optional = false)
    DocutoolsUser user
    @Column(nullable = false)
    String newEmail
    @Column(nullable = false)
    LocalDateTime timestamp = LocalDateTime.now()
    @Column(nullable = false)
    String verificationToken = UUID.randomUUID().toString()
    boolean verified = false

    ChangeEmailRequest() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ChangeEmailRequest that = (ChangeEmailRequest) o

        if (verified != that.verified) return false
        if (id != that.id) return false
        if (newEmail != that.newEmail) return false
        if (timestamp != that.timestamp) return false
        if (verificationToken != that.verificationToken) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (newEmail != null ? newEmail.hashCode() : 0)
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0)
        result = 31 * result + (verificationToken != null ? verificationToken.hashCode() : 0)
        result = 31 * result + (verified ? 1 : 0)
        return result
    }
}
