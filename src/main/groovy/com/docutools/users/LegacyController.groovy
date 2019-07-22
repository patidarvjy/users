package com.docutools.users

import com.docutools.users.resources.RegistrationDTO
import com.docutools.users.resources.VerificationDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import static org.springframework.http.HttpStatus.CREATED
/**
 * Mirroring some legacy requests to new ones.
 */
@RestController
class LegacyController {

    @Autowired RegistrationApi registrationApi
    @Autowired PreAuthenticationApi preAuthenticationApi

    @PostMapping('/api/v2/users/email')
    @ResponseStatus(CREATED)
    void registerNewUser(@RequestBody RegistrationDTO body) {
        registrationApi.registerNewUser(body)
    }

    @PostMapping('/api/v2/me/verify')
    void verifyUser(@RequestBody VerificationDTO body) {
        registrationApi.verify(body)
    }

    @PostMapping('/api/v2/me/passwordReset')
    void resetPassword(@RequestParam String email) {
        preAuthenticationApi.resetPassword(email)
    }

}
