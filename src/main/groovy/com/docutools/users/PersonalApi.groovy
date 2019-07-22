package com.docutools.users

import com.docutools.users.resources.ChangeEmailDTO
import com.docutools.users.resources.ChangePasswordDTO
import com.docutools.users.resources.OrganisationDTO
import com.docutools.users.resources.OrganisationPermissions
import com.docutools.users.resources.UserDTO
import com.docutools.users.resources.VerifyEmailAddressDTO
import com.docutools.config.security.OTPQRCodeLinkGeneration
import com.docutools.config.security.QRCodeGenerator
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

import static UserManager.getCurrentActor

/**
 * Provides read and modification access to the current user's personal data.
 */
@RestController
@RequestMapping(path = '/api/v2/me', produces = 'application/json')
class PersonalApi {

    static final Logger log = LoggerFactory.getLogger(PersonalApi)

    @Autowired
    SessionManager sessionManager
    @Autowired
    UserManager manager

    @Value('${docutools.maestroUrl:}')
    String maestroUrl

    @ApiOperation(value = "Get User Profile")
    @GetMapping
    UserDTO getProfile() {
        log.info('GET /api/v2/me by {}', currentActor)

        UserDTO me = new UserDTO(sessionManager.currentUser)
        if(me.permissions != null && me.permissions.contains(OrganisationPermissions.ViewMaestro)) {
            me.settings.maestroUrl = maestroUrl
        }
        return me
    }

    @ApiOperation(value = "Get User Organisation")
    @GetMapping('/organisation')
    OrganisationDTO getOrganisation() {
        log.info('GET /api/v2/me/organisation by {}', currentActor)

        new OrganisationDTO(sessionManager.currentUser.organisation,sessionManager.currentUser)
    }

    @ApiOperation(value = "Update User Profile", notes = "Required Attributes: none")
    @PatchMapping(consumes = 'application/json')
    UserDTO updateProfile(@RequestBody UserDTO body) {
        log.info('PATCH /api/v2/me by {}', currentActor)

        new UserDTO(manager.updateUser(sessionManager.currentUser, body))
    }

    @ApiOperation(value = "Update Email Address", notes = "Required Attributes: password, email")
    @PutMapping(path = '/email', consumes = 'application/json')
    void updateEmailAddress(@RequestBody ChangeEmailDTO body) {
        log.info('PUT /api/v2/me/email by {}', currentActor)

        manager.issueEmailChangeRequest(sessionManager.currentUser, body.password, body.newEmail)
    }

    @ApiOperation(value = "Verify New Email", notes = "Required Attributes: token")
    @PostMapping(path = '/email/verify', consumes = 'application/json')
    void verifyNewEmailAddress(@RequestBody VerifyEmailAddressDTO body) {
        log.info('POST /api/v2/me/email/verify by {}', currentActor)

        manager.verifyCurrentUsersEmailChange(body.token)
    }

    @ApiOperation(value = "Change Password", notes = "Required Attributes: oldPassword, newPassword")
    @PutMapping(path = '/password', consumes = 'application/json')
    void changePassword(@RequestBody ChangePasswordDTO body) {
        log.info('PUT /api/v2/me/password by {}', currentActor)

        manager.changeUsersPassword(sessionManager.currentUser, body.oldPassword, body.newPassword)
    }

    @ApiOperation(value = "Enable 2FA")
    @PostMapping(path = '/2fa', produces = 'text/plain')
    String enableTwoFactorAuthentication() {
        log.info('POST /api/v2/me/2fa by {}', currentActor)

        def otpSecret = manager.enableTwoFactorAuthentication(sessionManager.currentUser)
        OTPQRCodeLinkGeneration.createQRCodeLink(currentActor, otpSecret)
    }

    @ApiOperation(value = "Generate 2FA Pairing QrCode")
    @GetMapping(path = '/2fa/qr', produces = 'image/png')
    void generatePairingQrCode(HttpServletResponse response) {
        log.info('GET /api/v2/me/2fa/qr by {}', currentActor)

        def user = sessionManager.currentUser

        // When two factor authentication is disabled, no QR code can be generated
        if (!user.settings.twoFactorAuthEnabled) {
            response.status = HttpStatus.NO_CONTENT.value()
            return
        }

        response.setHeader("Content-Type", "image/png")
        QRCodeGenerator.writeToOutputStream(user.username, user.settings.twoFASecret, response.outputStream)
    }

    @ApiOperation(value = "Disable 2FA")
    @DeleteMapping(path = '/2fa')
    void disableTwoFactorAuthentication() {
        log.info('DELETE /api/v2/me/2fa by {}', currentActor)

        manager.disableTwoFactorAuthentication(sessionManager.currentUser)
    }
}
