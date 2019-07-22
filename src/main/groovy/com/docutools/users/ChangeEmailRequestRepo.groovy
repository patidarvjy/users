package com.docutools.users

import com.docutools.users.DocutoolsUser
import com.docutools.users.values.ChangeEmailRequest
import org.springframework.data.jpa.repository.JpaRepository

interface ChangeEmailRequestRepo extends JpaRepository<ChangeEmailRequest, UUID> {

    Optional<ChangeEmailRequest> findByVerificationToken(String token)
    List<ChangeEmailRequest> findByUser(DocutoolsUser user)

}