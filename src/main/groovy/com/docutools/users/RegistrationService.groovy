package com.docutools.users

import com.docutools.config.security.PasswordEncoder
import com.docutools.emails.EmailTemplateType
import com.docutools.emails.MailServer
import com.docutools.exceptions.ErrorCodes
import com.docutools.password.PasswordPolicies
import com.docutools.scheduler.SchedulerService
import com.docutools.scheduler.jobs.NotificationAfter48Hours
import com.docutools.subscriptions.SubscriptionRepository
import com.docutools.users.resources.RegistrationDTO
import com.docutools.users.values.Password
import com.docutools.users.values.PersonName
import com.docutools.users.values.UserType
import com.docutools.users.values.VerificationStatus
import com.docutools.utils.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

import javax.transaction.Transactional

import static com.docutools.exceptions.ErrorCodes.*
import static com.docutools.exceptions.ExceptionHelper.newBadRequestError
import static com.docutools.exceptions.ExceptionHelper.newConflictError
import static com.docutools.exceptions.ExceptionHelper.newUnauthorizedError
import static java.time.LocalDateTime.now

/**
 * Business logic for registering new users.
 */
@Service
@Transactional
class RegistrationService {

    @Autowired
    UserRepo userRepo
    @Autowired
    OrganisationRepo orgRepo
    @Autowired
    MailServer mailServer
    @Autowired
    PasswordEncoder pwEncoder
    @Autowired
    SubscriptionRepository subscriptionRepository
    @Autowired
    BlockedEmails blockedEmails
    @Autowired
    SchedulerService schedulerService
    @Autowired
    VerificationStatusHelper verificationStatusHelper
    @Autowired
    PasswordPolicies passwordPolicies

    @Value('${docutools.sustainOrganization.id:edaafcd8-a1de-11e7-abc4-cec278b6b50a}')
    UUID sustainOrganizationId
    @Value('${docutools.sustainOrganization.name:Sustain Organization}')
    String sustainOrganizationName
    @Value('${docutools.sustainOrganization.cc:cc}')
    String sustainOrganizationCc

    /**
     * Checks if the specified email address is already in use.
     *
     * @param email email address
     * @return {@link true} if already in use
     */
    boolean isEmailInUse(String email) {
        def user = userRepo.findByUsernameIgnoreCase(email)
        user.isPresent() && user.get().verificationStatus.verified
    }

    /**
     * Registers a new docutools user for a new {@link Organisation}. The new user will receive an email with an
     * invitation link.
     *
     * @param registration user's basic contact data (at least email, organisation name and country code).
     */
    @Transactional
    void registerNewUser(RegistrationDTO registration) {

        validateRegistrationDto(registration)

        // When the email address was already used for registration, either resent an email when not verified yet
        // or give a conflict error
        def result = userRepo.findByUsernameIgnoreCase(registration.email)
        if (result.present) {
            def existingUser = result.get()
            if (existingUser.verificationStatus.verificationRequired) {
                //Update user info
                existingUser.setName(new PersonName(firstName: registration.firstName ?: '', lastName: registration.lastName ?: ''))
                if (registration.language) existingUser.settings.language = registration.language
                if (registration.phone) existingUser.phone = registration.phone
                if (existingUser.organisationOwner) {
                    existingUser.organisation.name = registration.organisationName
                    existingUser.organisation.cc = registration.countryCode
                }
                userRepo.save(existingUser)
                // Resend Email Invitation
                mailServer.sendEmail(EmailTemplateType.Register, existingUser)
                return
            } else {
                throw newConflictError()
            }
        }

        def organisation
        if (Validator.isSustainUser(registration.email)) {
            organisation = getOrCreateSustainOrganization()
        } else {
            // Create new organisation
            organisation = orgRepo.save new Organisation(name: registration.organisationName, cc: registration.countryCode)
        }

        // Create new user
        def newUser = new DocutoolsUser(username: registration.email, organisation: organisation, termsAndConditionsAccepted: true, privacyPolicyAccepted: true)
        newUser.name = new PersonName(firstName: registration.firstName ?: '', lastName: registration.lastName ?: '')
        if (registration.language) newUser.settings.language = registration.language
        if (registration.phone) newUser.phone = registration.phone
        def user = userRepo.saveAndFlush(newUser)

        if (Validator.isSustainUser(registration.email) && organisation.owner != null) {
            organisation.members.add(user)
        } else {
            // Set user to organisation owner
            organisation.owner = user
        }
        orgRepo.save organisation
        // Send email invitation
        mailServer.sendEmail(EmailTemplateType.Register, user)
    }

    def validateRegistrationDto(RegistrationDTO registrationDTO) {
        if (StringUtils.isEmpty(registrationDTO.email) || !Validator.isValidEmail(registrationDTO.email)) {
            throw newBadRequestError(INVALID_RESOURCE, "Email")
        }
        if (blockedEmails.isBlocked(registrationDTO.email)) {
            throw newBadRequestError(EMAIL_BLOCKED)
        }
        if (StringUtils.isEmpty(registrationDTO.organisationName)) {
            throw newBadRequestError(MISSING_REQUIRED_VALUE, "Organization name")
        }
        if (StringUtils.isEmpty(registrationDTO.countryCode)) {
            throw newBadRequestError(MISSING_REQUIRED_VALUE, "Country code")
        }
    }
/**
 * Loads the user associated with the verification token and tries to verify the mail address and set a new
 * password.
 *
 * In case that the token expired a new invitation link is sent to the mail.
 *
 * @param token email verification token.
 * @param password users first password.
 * @throws com.docutools.apierrors.ApiException when token is invalid or expired, user is already verified
 * or the password does not meet the security standards.
 */
    void verify(String token, String password) {
        // Find account
        def user = userRepo.findByVerificationStatusToken(token)
                .orElseThrow({ newBadRequestError(INVALID_RESOURCE, String.format("Verification Token: %s", token)) })
        // Check if verification can be executed
        if (user.verificationStatus.verified && !user.verificationStatus.passwordReset)
            throw newBadRequestError(VERIFICATION_NOT_REQUIRED)
        if (user.verificationStatus.expiryTime.isBefore(now())) {
            // Update Verification Token
            verificationStatusHelper.updateExpiryTime(user)
            // Send re-invitation email
            mailServer.sendEmail(EmailTemplateType.TokenExpired, user)
            throw newBadRequestError(EXPIRED_RESOURCE, "Verification Token")
        }
        // Verify the password
        def newPassword = pwEncoder.hashPassword password
        def passwordPolicy = passwordPolicies.get(user.organisation.passwordPolicy)
        if(!passwordPolicy.validate(password, pwEncoder, user)) {
            throw newBadRequestError(WEAK_PASSWORD)
        }
        // Reset the verification status as the verify/reset has succeeded
        user.verificationStatus = new VerificationStatus()
        user.verificationStatus.verified = true
        user.verificationStatus.expiryTime = now()
        // Set password
        user.password = newPassword

        password = null // prevent any downstream leaks

        setVerificationStatus(user)
        schedulerService.schedule(new NotificationAfter48Hours(user.name.firstName+" "+user.name.lastName, user.id))
    }

    DocutoolsUser setVerificationStatus(DocutoolsUser user) {
        // Verify
        user.verificationStatus.verificationRequired = false
        // Disable two factor authentication, since when a password reset was issued 2FA could have been the reason.
        user.settings.disbaleTwoFactorAuth()
        // Disable SMS MFA
        user.settings.disableSMSFactorAuth()
        // Create Account when Owner
        if (user.organisationOwner && user.account == null) {
            def sub = user.organisation.subscription
            sub.freeAccount.orElseGet { sub.newAccount().orElse(null) }?.assign(user)
            subscriptionRepository.save(sub)
        }
        if (!user.account && user.getType() != UserType.SAML) {
            def sub = user.organisation.subscription
            sub.freeAccount.ifPresent {
                it.assign(user)
            }
            subscriptionRepository.save(sub)
        }
        userRepo.save user
    }

    /**
     * Resets the users verification status to required and disables two factor authentication. Then sends a new
     * invitation link to the users email address.
     *
     * @param email users email address.
     * @throws com.docutools.apierrors.ApiException when the email address is wrong.
     */
    void resetUsersPassword(String email) {
        // Find account
        def user = userRepo.findByUsernameIgnoreCase(email)
                .orElseThrow({
            newBadRequestError(RESOURCE_NOT_FOUND, "Email")
        })
        user.verificationStatus.passwordReset = true
        user.verificationStatus.expiryTime = now().plusDays(7)
        // Save and send reset link
        userRepo.save(user)
        mailServer.sendEmail(EmailTemplateType.ForgotPassword, user)
    }

    private Organisation getOrCreateSustainOrganization() {
        def org = orgRepo.findById(sustainOrganizationId)
        if (org.isPresent()) {
            return org.get()
        } else {
            Organisation organisation = new Organisation(name: sustainOrganizationName, cc: sustainOrganizationCc)
            orgRepo.save(organisation)
        }
    }

    public void migrateUserToSAML(DocutoolsUser user, String idpLink){
        Optional<Organisation> org = orgRepo.findByIdpLink(authenticationRequest.getIdp())
        if(!org.isPresent()){
            throw newUnauthorizedError("This is not a SAML User.");
        }
        user.setOrganisation(org.get())
        user.setOrganisationName(null)
        user.setEmail(authenticationRequest.getEmail())
        setVerificationStatus(user)
        user.setType(UserType.SAML)
        userRepo.save(user)
    }
}
