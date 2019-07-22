package com.docutools.test

import com.docutools.users.DocutoolsUser
import com.docutools.users.Organisation
import com.docutools.config.security.PasswordEncoder

import javax.persistence.Entity

import static com.docutools.test.AlternativeFacts.email

/**
 * Extends the {@link DocutoolsUser} to automatically fill its data and set it to verified. Therefore needs a
 * password and {@link PasswordEncoder} provided.
 */
//@Entity
class DocutoolsTestUser extends DocutoolsUser {

    String clearTextPassword

    /**
     * Required by hibernate.
     */
    protected DocutoolsTestUser() {
    }

    DocutoolsTestUser(String clearTextPw, PasswordEncoder pwEncoder, Organisation organisation) {
        username = email()
        this.organisation = organisation
        verificationStatus.verificationRequired = false
        password = pwEncoder.hashPassword(clearTextPw)
        this.clearTextPassword = clearTextPw
    }

}
