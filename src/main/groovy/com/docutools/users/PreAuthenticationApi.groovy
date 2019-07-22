package com.docutools.users

import com.docutools.users.resources.TwoFactorAuthOptionsDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Requests for unauthenticated users.
 */
@RestController
@RequestMapping('/api/v2')
class PreAuthenticationApi {

    static final Logger log = LoggerFactory.getLogger(PreAuthenticationApi)

    @Autowired UserRepo userRepo
    @Autowired RegistrationService regService

    @GetMapping('/users/email/2fa')
    @PreAuthorize('isAnonymous()')
    boolean isTwoFactorAuthenticationRequired(@RequestParam String email) {
        log.info('/api/v2/users/2fa?email={}', email)

        userRepo.findByUsernameIgnoreCase(email)
                .map({it.settings.twoFactorAuthEnabled})
                .orElse(false)
    }

    @GetMapping('/twoFactor')
    @PreAuthorize('isAnonymous()')
    TwoFactorAuthOptionsDTO listTwoFactorAuthenticationOptions(@RequestParam String email) {
        log.info('/api/v2/twoFactor?email={}', email)

        return userRepo.findByUsernameIgnoreCase(email)
                .map({ new TwoFactorAuthOptionsDTO(it.settings.twoFactorAuthEnabled, it.settings.smsFactorAuthEnabled) })
                .orElse(new TwoFactorAuthOptionsDTO(false, false))
    }

    @PostMapping('/passwordReset')
    @PreAuthorize('isAnonymous()')
    void resetPassword(@RequestParam String email) {
        log.info('POST /api/v2/passwordReset?email={}', email)

        regService.resetUsersPassword(email)
    }

}
