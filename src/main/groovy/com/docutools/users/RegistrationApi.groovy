package com.docutools.users

import com.docutools.password.PasswordPolicies
import com.docutools.password.PasswordPolicy
import com.docutools.users.resources.RegistrationDTO
import com.docutools.users.resources.VerificationDTO
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import static java.lang.Enum.valueOf
import static org.springframework.http.HttpStatus.CREATED

/**
 * Contains requests for registration and authentication.
 */
@RestController
@RequestMapping(path = '/api/v2', produces = 'application/json')
class RegistrationApi {

    static final Logger log = LoggerFactory.getLogger(RegistrationApi)

    @Autowired RegistrationService service
    @Autowired UserRepo userRepo
    @Autowired PasswordPolicies passwordPolicies

    @ApiOperation(value = "Check if Email is in Use")
    @GetMapping(path = '/users/email')
    def boolean isEmailInUse(@RequestParam String email) {
        log.info('GET /api/v2/users/email?email={}', email)

        service.isEmailInUse(email)
    }

    @ApiOperation(value = "Register New User", notes = "Required Attributes: email, organisationName, countryCode")
    @PreAuthorize('isAnonymous()')
    @PostMapping(path = '/register', consumes = 'application/json')
    @ResponseStatus(CREATED)
    def void registerNewUser(@RequestBody RegistrationDTO body) {
        log.info('POST /api/v2/register')
        log.debug('Request Body: {}', body)

        service.registerNewUser(body)
    }

    @ApiOperation(value = "Verify User", notes = "Required Attributes: token, password")
    @PreAuthorize('isAnonymous()')
    @PostMapping(path = '/register/verify', consumes = 'application/json')
    def void verify(@RequestBody VerificationDTO body) {
        log.info('POST /api/v2/register/verify')
        log.debug('Request Body: {}', body)

        service.verify(body.token, body.password)
    }

    @ApiOperation(value = "Get User's Password Policy.")
    @GetMapping(path = "/register/passwordPolicy")
    PasswordPolicy getPolicy(@RequestParam(name = "username", required = true) String username) {
        return userRepo.findByUsernameIgnoreCase(username)
            .map({passwordPolicies.get(it.organisation.passwordPolicy)})
            .orElse(passwordPolicies.getDefault());
    }

}
