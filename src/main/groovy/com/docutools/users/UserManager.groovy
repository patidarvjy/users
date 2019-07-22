package com.docutools.users

import com.docutools.avatar.AvatarService
import com.docutools.config.security.PasswordEncoder
import com.docutools.emails.ChangeEmailService
import com.docutools.emails.EmailTemplateType
import com.docutools.emails.MailServer
import com.docutools.password.PasswordPolicies
import com.docutools.roles.PermissionManager
import com.docutools.roles.Privilege
import com.docutools.services.projects.ProjectApiClient
import com.docutools.storage.StorageEngine
import com.docutools.subscriptions.Account
import com.docutools.subscriptions.SubscriptionRepository
import com.docutools.team.TeamManager
import com.docutools.team.TeamMembershipRepo
import com.docutools.users.resources.NotificationDTO
import com.docutools.users.resources.UserDTO
import com.docutools.users.resources.UserFilter
import com.docutools.users.values.PersonName
import com.docutools.users.values.UserSettings
import com.docutools.users.values.UserType
import com.docutools.users.values.VerificationStatus
import com.docutools.utils.Validator
import com.docutools.vcard.vCardGenerator
import ezvcard.VCardVersion
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.JpaSort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Collectors

import static com.docutools.exceptions.ErrorCodes.EMAIL_BLOCKED
import static com.docutools.exceptions.ErrorCodes.INCORRECT_PASSWORD
import static com.docutools.exceptions.ErrorCodes.INVALID_RESOURCE
import static com.docutools.exceptions.ErrorCodes.NO_LICENSES
import static com.docutools.exceptions.ErrorCodes.RESOURCE_NOT_FOUND
import static com.docutools.exceptions.ErrorCodes.SAML_CANT_CHANGE_EMAIL
import static com.docutools.exceptions.ErrorCodes.SAML_CANT_CHANGE_PASSWORD
import static com.docutools.exceptions.ErrorCodes.WEAK_PASSWORD
import static com.docutools.exceptions.ExceptionHelper.newBadRequestError
import static com.docutools.exceptions.ExceptionHelper.newConflictError
import static com.docutools.exceptions.ExceptionHelper.newForbiddenError
import static com.docutools.exceptions.ExceptionHelper.newInputValidationError
import static com.docutools.exceptions.ExceptionHelper.newResourceNotFoundError

/**
 * Business logic for managing users as organisation administrator.
 */
@Service
@Transactional
class UserManager {

    private static final Logger log = LoggerFactory.getLogger(UserManager)
    private static
    final String[] userSortProperties = ['id', 'name', 'license', 'licensed_since', 'licensed_until', 'admin', 'projectCreator', 'paid']

    @Autowired
    OrganisationRepo orgRepo
    @Autowired
    UserRepo userRepo
    @Autowired
    SubscriptionRepository subscriptionRepository
    @Autowired
    SessionManager sessionManager
    @Autowired
    MailServer mailServer
    @Autowired
    ChangeEmailService changeEmailService
    @Autowired
    PasswordEncoder pwEncoder
    @Autowired
    PermissionManager permissionManager
    @Autowired
    TeamManager teamManager
    @Value('${docutools.users.avatar.fileSizeLimitInKb:10240}')
    int avatarFileSizeLimitInKb
    @Autowired
    StorageEngine storageEngine

    @Autowired
    private ProjectApiClient projectApiClient
    @Autowired
    private TeamMembershipRepo teamMembershipRepo
    @Autowired
    private AvatarService avatarService
    @Autowired
    private RegistrationService registrationService

    @Autowired
    private BlockedEmails blockedEmails
    @Autowired
    private PasswordPolicies passwordPolicies


    /**
     * Gets the current users organisation and invites a new user to it by sending an invitation link to the specified
     * email.
     *
     * @param email the new user's email address.
     * @return the newly created {@link DocutoolsUser} entity.
     * @throws com.docutools.apierrors.ApiException when the email is invalid ({@code null} or doesn't contain
     * an {@code @} symbol) or the email already exists in a database.
     */
    DocutoolsUser inviteNewUser(UserDTO userDto, Organisation organisation) {
        def  email = userDto.email
        if (StringUtils.isEmpty(email) || !Validator.isValidEmail(email)) {
            throw newBadRequestError(INVALID_RESOURCE, "Email: <$email>")
        }
        if (userRepo.findByUsernameIgnoreCase(email).present) {
            throw newConflictError("Email already exists")
        }
        def language
        //Current user can be unauthenticated if it comes from SAML login
        if (userDto.settings?.language != null) {
            language = userDto.settings.language
        } else if (userDto.type != UserType.SAML &&
                SecurityContextHolder?.context?.authentication?.authenticated) {
            language = sessionManager.currentUser.settings.language
        } else {
            language = 'en'
        }

        String username = userDto.username ?: userDto.email
        def user = new DocutoolsUser(username: username, organisation: organisation,
                settings:  new UserSettings(admin: false, projectCreator: false, language: language))
        if (StringUtils.isEmpty(userDto.firstName) && StringUtils.isEmpty(userDto.lastName)) {
            setPersonName(user)
        } else {
            user.name = new PersonName(lastName: userDto.lastName ?: '', firstName: userDto.firstName ?: '')
        }
        if(userDto.type){
            user.type = userDto.type
        }
        // Take given ID and replace with randomly generated.
        if(userDto.userId) {
            // Check if ID is not in use yet
            if(userRepo.existsById(userDto.userId)) {
                throw newConflictError("User Id already exists")
            }
            user.id = userDto.userId
        }
        def newUser = userRepo.save user
        if (user.type == UserType.SAML) {
            return registrationService.setVerificationStatus(newUser)
        } else if(blockedEmails.isBlocked(newUser.username)) {
            throw newBadRequestError(EMAIL_BLOCKED)
        }
        def currentUser = sessionManager.getCurrentUser()
        mailServer.sendEmail(EmailTemplateType.Invitation,
                [inviter: currentUser.name, organisation_name: currentUser.getCompanyName()], newUser)
        return newUser
    }

    /**
     * Extract and Set FirstName and LastName of user from email
     * @param docutoolsUser
     * @return
     */
    def setPersonName(DocutoolsUser docutoolsUser) {
        def emailParts= docutoolsUser.username.split("@")
        if(emailParts.length>1){
            def emailFirstPart = emailParts[0].split("[.]")
            if(emailFirstPart.length>1){
                docutoolsUser.name.setFirstName(emailFirstPart[0])
                docutoolsUser.name.setLastName(emailFirstPart[1])
            }else {
                docutoolsUser.name.setFirstName(emailFirstPart[0])
                docutoolsUser.name.setLastName(emailParts[1].split("[.]")[0])
            }
        }else {
            docutoolsUser.name.setFirstName(docutoolsUser.username)
        }
    }
/**
     * If a user with the specified email address already exists, it is returned, otherwise a new user with a new
     * organisation is created in the database and an invitation email is sent.
     *
     * @param email the user's email.
     * @param organisationId ID of the organisation the user shall belong too, when {@code null} a new one is created.
     * @param id ID of the new DocutoolsUser
     * @return the {@link DocutoolsUser}.
     */
    DocutoolsUser loadOrCreateUser(String email, UUID organisationId = null, String organizationName = '', UUID projectId = null, UUID id = UUID.randomUUID()) {
        if (StringUtils.isEmpty(email) || !Validator.isValidEmail(email)) {
            throw newBadRequestError(INVALID_RESOURCE, "Email: <$email>")
        }
        if(!organizationName){
            organizationName = ''
        }
        def result = userRepo.findByUsernameIgnoreCase(email)
        if (result.present) {
            return result.get()
        } else if(blockedEmails.isBlocked(email)) {
            throw newBadRequestError(EMAIL_BLOCKED)
        }

        def currentUser = sessionManager.currentUser

        DocutoolsUser user

        if (organisationId) {
            if (currentUser.organisation.id != organisationId) {
                throw newForbiddenError()
            }
            def organisation = orgRepo.findById(organisationId)
                    .orElseThrow {
                newBadRequestError(RESOURCE_NOT_FOUND, "OragnisationId")
            }
            def docutoolsUser = new DocutoolsUser(id: id, organisation: organisation, username: email,
                    settings: new UserSettings(admin: false, projectCreator: false, language: currentUser.settings.language))
            setPersonName(docutoolsUser)
            user = userRepo.save docutoolsUser
        } else {
            def organisation = orgRepo.save new Organisation(name: organizationName)
            def docutoolsUser = new DocutoolsUser(id: id, organisation: organisation, username: email,
                    settings:  new UserSettings(language: currentUser.settings.language))
            setPersonName(docutoolsUser)
            user = userRepo.save docutoolsUser
            user.settings.admin = true
            organisation.owner = user
            orgRepo.save organisation
        }

        if (projectId) {
            def project = projectApiClient.getProject(projectId)
            mailServer.sendEmail(EmailTemplateType.InviteToProject, [inviter: currentUser.name, projectName: project?.name, receiver_mail: user.email], user)
        } else {
            mailServer.sendEmail(EmailTemplateType.Invitation,
                    [inviter: currentUser.name, organisation_name: currentUser.getCompanyName()], user)
        }
        user.isNewCreated = true
        return user
    }

    Page<DocutoolsUser> listOrganisationUsers(int page = 0,
                                              int pageSize = 10,
                                              String sort = 'id',
                                              Sort.Direction sortDir = Sort.Direction.ASC,
                                              UserFilter filter = UserFilter.Any,
                                              String search = '') {
        if (page < 0) {
            throw newBadRequestError(INVALID_RESOURCE, "page (Must be a positive number)")
        }
        if (pageSize <= 0) {
            throw newBadRequestError(INVALID_RESOURCE, "pageSize (Must be greater than 0)")
        }
        if (!userSortProperties.contains(sort)) {
            throw newBadRequestError(INVALID_RESOURCE, "sort")
        }
        def org = sessionManager.currentUser.organisation
        def pageRequest
        if(filter != UserFilter.Any) {
            pageRequest = new PageRequest(page, pageSize, toSort(sortDir, sort))
        } else {
            pageRequest = getNativeSortPageRequest(page, pageSize, sortDir, sort)
        }
        if (search) {
            search = search.toUpperCase()
        }
        switch (filter) {
            case UserFilter.WithoutLicense:
                return userRepo.findByOrganisationAndLicense(org, "%$search%", LocalDate.now().minusDays(1), false, pageRequest)
            case UserFilter.Licensed:
                return userRepo.findByOrganisationAndLicense(org, "%$search%", LocalDate.now().minusDays(1), true, pageRequest)
            default: return userRepo.findByOrganisationId(org.id, "%$search%", pageRequest)
        }
    }

    private static Sort toSort(Sort.Direction sortDir, String sort){
        if (sort == 'name')
            Sort.by(new Sort.Order(sortDir, 'name.lastName').ignoreCase(),
                    new Sort.Order(sortDir, 'name.firstName').ignoreCase())
        else if (sort == 'license')
            Sort.by(sortDir, 'account.subscription.type')
        else if (sort == 'licensed_since')
            Sort.by(sortDir, 'account.subscription.since')
        else if (sort == 'licensed_until')
            Sort.by(sortDir, 'account.subscription.until')
        else if (sort == 'admin')
            Sort.by(new Sort.Order(sortDir, 'settings.admin'),
                    new Sort.Order(sortDir, 'settings.projectCreator'))
        else if (sort == 'projectCreator')
            Sort.by(new Sort.Order(sortDir, 'settings.projectCreator'),
                    new Sort.Order(sortDir, 'settings.admin'))
        else if (sort == 'paid')
        // Paid is not a field in the tables or objects, it is created when translating the object to a response
        // Sorting by type is currently the only practical way to sort, because anything that is not 'Test' is paid
            Sort.by(sortDir, 'account.subscription.type')
        else
            Sort.by(sortDir, sort)
    }

    //Licensed-Since needs a different order by and sort to display it correctly
    private static PageRequest getNativeSortPageRequest(int page, int pageSize, Sort.Direction sortDir, String sort){
        if(sort == 'licensed_since')
            PageRequest.of(page, pageSize, JpaSort.unsafe(sortDir, 'COALESCE(subscription.since, created)'))
        else
            new PageRequest(page, pageSize, toNativeSortProperties(sortDir, sort))
    }

    //Different sort strings are needed for the native query
    private static Sort toNativeSortProperties(Sort.Direction sortDir, String sort){
        if (sort == 'name')
            Sort.by(new Sort.Order(sortDir, 'last_name').ignoreCase(),
                    new Sort.Order(sortDir, 'first_name').ignoreCase())
        else if (sort == 'license')
            Sort.by(sortDir, 'subscription.type')
        else if (sort == 'licensed_since')
            Sort.by(sortDir, 'subscription.since')
        else if (sort == 'licensed_until')
            Sort.by(sortDir, 'subscription.until')
        else if (sort == 'admin')
            Sort.by(new Sort.Order(sortDir, 'admin'),
                    new Sort.Order(sortDir, 'project_creator'))
        else if (sort == 'projectCreator')
            Sort.by(new Sort.Order(sortDir, 'project_creator'),
                    new Sort.Order(sortDir, 'admin'))
        else if (sort == 'paid')
            // Paid is not a field in the tables or objects, it is created when translating the object to a response
            // Sorting by type is currently the only practical way to sort, because anything that is not 'Test' is paid
            Sort.by(sortDir, 'subscription.type')
        else
            Sort.by(sortDir, sort)
    }

    /**
     * Tries to load the {@link DocutoolsUser} with the specified id or end exceptionally.
     *
     * @param id of the requested user.
     * @return requested user.
     * @throws com.docutools.apierrors.ApiException when there is no such user.
     */
    DocutoolsUser loadUserById(UUID id) {
        userRepo.findById(id)
                .orElseThrow({ newResourceNotFoundError(String.format("User with id: %s", id.toString()))})
    }

    DocutoolsUser getUserByEmail(String email) {
        userRepo.findByUsernameIgnoreCase(email)
                .orElseThrow({ newResourceNotFoundError(String.format("email: %s", email)) })
    }

    /**
     * Gets the name of the currently logged in user. If no user is currently logged in it returns the string
     * {@code "Anonymous"}.
     *
     * @return current user's name or anonymous.
     */
    static String getCurrentActor() {
        SecurityContextHolder.getContext()?.getAuthentication()?.getPrincipal()?.toString() ?: 'Anonymous'
    }

    /**
     * Updates the specified user with all non-null fields in the update object. Fields permitted to update are:
     * <ul>
     *     <li>first/last name</li>
     *     <li>phone</li>
     *     <li>job title</li>
     *     <li>settings: language and time zone</li>
     * </ul>
     * All other fields will be ignored.
     *
     * @param user the user entity to be updated.
     * @param update the new values.
     * @return updated user entity.
     */
    DocutoolsUser updateUser(DocutoolsUser user, UserDTO update) {
        def currentUser = sessionManager.currentUser
        if(update.updatesOrganisationNameOnly()){
            canUpdateOrganisationName(user)
        } else {
            canUpdate(user)
        }
        // Update fields
        if (update.firstName != null) user.name.firstName = update.firstName
        if (update.lastName != null) user.name.lastName = update.lastName
        if (update.phone != null) user.phone = update.phone
        if (update.fax != null) user.fax = update.fax
        if (update.jobTitle != null) user.jobTitle = update.jobTitle
        if (update.department != null) user.department = update.department
        if (update.internalId != null) user.internalId = update.internalId
        if (update.street != null) user.street = update.street
        if (update.zip != null) user.zip = update.zip
        if (update.city != null) user.city = update.city
        if(update.organisationNameId != null) {
            if(update.organisationNameId == user.organisation.id) {
                user.organisationName = null
            } else {
                OrganisationName newName = user.organisation.names.find { it.id == update.organisationNameId }
                if (newName) {
                    user.organisationName = newName
                }
            }
        }
        if (update.settings?.language != null) user.settings.language = update.settings.language
        if (update.settings?.timeZone) user.settings.timeZone = update.settings.timeZone
        if (update.settings?.projectCreator != null) {
            if (!currentUser.admin) {
                // Only admins can grant or revoke project creator from users
                throw newForbiddenError()
            }
            user.settings.projectCreator = update.settings.projectCreator
        }
        if (update.settings?.admin != null) {
            if (!currentUser.isOrganisationOwner())
                // Only the organisation owner can grant or revoke admin to other users
                throw newForbiddenError()
            user.settings.admin = update.settings.admin
        }
        if (update.settings?.savePhotosOnDevice != null) {
            user.settings.savePhotosOnDevice = update.settings.savePhotosOnDevice
        }
        if (update.settings?.transcribeAudios != null) {
            user.settings.transcribeAudios = update.settings.transcribeAudios
        }
        if (update.active != null) {
            if (!currentUser.admin) {
                // Only admins can update user's activation status
                throw newForbiddenError()
            }
            user.active = update.active
        }
        if(update.termsAndConditionsAccepted && update.privacyPolicyAccepted) {
            user.termsAndConditionsAccepted = update.termsAndConditionsAccepted
            user.privacyPolicyAccepted = update.privacyPolicyAccepted
        }
        // Save entity
        userRepo.save(user)
    }

    /**
     * Updates all users with the same properties.
     *
     * @param ids IDs of the users to update.
     * @param update properties to update.
     * @return updated {@link DocutoolsUser}s
     */
    @Transactional
    List<DocutoolsUser> updateAll(List<UUID> ids, UserDTO update) {
        ids.collect { loadUserById(it) }
                .collect { updateUser(it, update) }
    }

    /**
     * Verified the password against the currently logged in user and then starts an email change. Therefore the user
     * will receive an email to the new address with a verification link.
     *
     * @param user to change issue the email change request.
     * @param password user's password to verify his identity.
     * @param newEmail the new email address for the user.
     */
    void issueEmailChangeRequest(DocutoolsUser user, String password, String newEmail) {
        if(user.type == UserType.SAML){
            throw newBadRequestError(SAML_CANT_CHANGE_EMAIL)
        }
        changeEmailService.changeEmailAddress(user.username, password, newEmail)
    }

    /**
     * Checks if there is a change email request for a user with the specified token. If the token is valid and did not
     * expire the change email request is performed and the user can login with his new email address.
     *
     * @param token change email verification token (sent to the users new address per mail).
     */
    void verifyCurrentUsersEmailChange(String token) {
        changeEmailService.verifyNewEmailAddress(token)
    }

    /**
     * Changes the users password to a new one.
     *
     * @param user to change the password for.
     * @param oldPassword user's old password, to verify the identity.
     * @param newPassword user's new password.
     * @throws com.docutools.apierrors.ApiException when the old password is wrong or the new password is too
     * weak.
     */
    void changeUsersPassword(DocutoolsUser user, String oldPassword, String newPassword) {
        if (user.type == UserType.SAML) {
            throw newBadRequestError(SAML_CANT_CHANGE_PASSWORD)
        }

        if (!pwEncoder.checkPassword(user.password, oldPassword)) {
            throw newBadRequestError(INCORRECT_PASSWORD)
        }
        // Prevent any downstream leaks
        oldPassword = null

        // Validate new Password
        def password = pwEncoder.hashPassword(newPassword)
        def passwordPolicy = passwordPolicies.get(user.organisation.passwordPolicy)
        if(!passwordPolicy.validate(newPassword, pwEncoder, user)) {
            throw newBadRequestError(WEAK_PASSWORD)
        }

        user.password = password
        userRepo.save user
    }

    void generateTwoFASecret(DocutoolsUser user) {
        canUpdate(user)
        user.settings.generateTwoFASecretIfNecessary()
        userRepo.save user
    }

    /**
     * Enables two factor authentication for the specified user and generated a new OTP secret.
     *
     * @param user to enable two factor for.
     * @return the new OTP secret.
     */
    String enableTwoFactorAuthentication(DocutoolsUser user) {
        canUpdate(user)
        user.settings.enableTwoFactorAuth()
        userRepo.save user
        return user.settings.twoFASecret
    }

    /**
     * Disables two factor authentication for the specified user and removes the OTP secret.
     *
     * @param user to disable two factor for.
     */
    void disableTwoFactorAuthentication(DocutoolsUser user) {
        canUpdate(user)
        user.settings.disbaleTwoFactorAuth()
        user.settings.disableSMSFactorAuth()
        userRepo.save user
    }

    /**
     * Enables sms factor authentication for the specified user.
     * @param user
     */
    void enableSMSFactorAuthentication(DocutoolsUser user) {
        canUpdate(user)
        user.settings.enableSMSFactorAuth()
        userRepo.save user
    }

    /**
     * Disables sms factor authentication for the specified user
     * @param user
     */
    void disableSMSFactorAuthentication(DocutoolsUser user) {
        canUpdate(user)
        user.settings.disableSMSFactorAuth()
        userRepo.save user
    }


    /**
     * Exports a {@link DocutoolsUser}s contact information to the VCard Version 3 format.
     * VCard Version 3 is used over Version 4 due the compatibility with other softwares and systems.
     *
     * @param user {@link DocutoolsUser}
     * @return VCard Version 3
     */
    String exportVCard(DocutoolsUser user) {
        log.debug("Exporting user <$user> to VCard...")

        return vCardGenerator.generateVCard(VCardVersion.V3_0, user, avatarService)
    }

    /**
     * Checks if the {@link SessionManager#getCurrentUser()} is allowed to update the specifed user. If not a
     * {@link com.docutools.apierrors.ApiException} is thrown.
     *
     * @param user user to test for.
     */
    private void canUpdate(DocutoolsUser user) {
        def currentUser = sessionManager.currentUser
        if (currentUser.organisation.id != user.organisation.id) {
            throw newForbiddenError('Only users in the same organisation can update each other.')
        }
        if (currentUser.id != user.id && !currentUser.admin) {
            throw newForbiddenError("User does not have the authority to update this user.")
        }
        if (user.isOrganisationOwner() && user.id != currentUser.id) {
            throw newForbiddenError('The organisation owner can only update herself.')
        }
        if (!currentUser.isOrganisationOwner() && currentUser.admin && user.admin && currentUser.id != user.id) {
            throw newForbiddenError('Admins cannot update other admin.')
        }
    }

    /**
     * Checks if the {@link SessionManager#getCurrentUser()} is allowed to update the organisation name of the
     * specific user, if not a {@link com.docutools.apierrors.ApiException} is thrown.
     *
     * @param user user to test for.
     */
    private void canUpdateOrganisationName(DocutoolsUser user){
        def currentUser = sessionManager.currentUser
        if (currentUser.organisation.id != user.organisation.id) {
            throw newForbiddenError('Only users in the same organisation can update each other.')
        }
        if (currentUser.id != user.id && !currentUser.admin) {
            throw newForbiddenError("User does not have the authority to update this user.")
        }
    }

    void notifyTaskAssigned(DocutoolsUser targetUser, NotificationDTO body){
        if(targetUser.active) {
            if(teamManager.isUserActiveOrPrivileged(body.getProjectId(), targetUser.id)) {
                if (currentUserCanAssignTasks(body.getProjectId())) {
                    log.info("Not sending this Email anymore!")
                } else {
                    throw unprivilegedError("Notification EMail for Task Assigned", Privilege.CreateTasks, Privilege.DelegateTasks, Privilege.ManageTasks)
                }
            }
        }
    }

    boolean currentUserCanAssignTasks(UUID projectId){
        return permissionManager.checkPrivilege(projectId,
                [Privilege.CreateTasks, Privilege.DelegateTasks, Privilege.ManageTasks],
                true).isCheck()
    }

    Page<DocutoolsUser> listUsersWithinAllOrg(int page = 0,
                                              int pageSize = 10,
                                              String search = '') {
        if (page < 0) {
            throw newInputValidationError("<page> param must be equal or greater than <0> not <$page>!")
        }
        if (pageSize <= 0) {
            throw newInputValidationError("<pageSize> param must be greater than <0> not <$pageSize>!")
        }
        def pageRequest = PageRequest.of(page, pageSize)
        if (search) {
            search = search.toUpperCase()
        }
        def projectIds = projectApiClient.getAllProjectsInCurrentOrganisation().stream()
                .filter{project->permissionManager.checkPrivilege(project.id,Collections.singletonList(Privilege.ViewTeam),true).check}
                .map{project->project.id}.collect(Collectors.toList())
        userRepo.findUsers(projectIds,
                        sessionManager.currentUser.organisation.id,"%$search%",pageRequest)
    }

    void reSendInvitationEmail(UUID id) {
        def user = loadUserById(id)
        if (user.verificationStatus?.verificationRequired && (!user.lastInvitationTime ||
                ZonedDateTime.now(ZoneId.of("UTC")).isAfter(user.lastInvitationTime.plusMinutes(10)))) {
            user.lastInvitationTime = ZonedDateTime.now(ZoneId.of("UTC"))
            user.verificationStatus = new VerificationStatus()
            userRepo.save(user)
            mailServer.sendEmail(EmailTemplateType.Invitation,
                    [inviter: sessionManager.currentUser.name, organisation_name: sessionManager.currentUser.getCompanyName()], user)
        }
    }

    void removeLicense(DocutoolsUser user) {
        if(sessionManager.getCurrentUser().getId().equals(user.getId()))
            return
        if(user.hasActiveAccount()) {
            def account = user.account
            account.removeAssignment()
            userRepo.save(user)
            log.debug("Removed Account from User $user.name ($user.id).")
        }
    }

    Account assignLicense(DocutoolsUser user) {
        if(user.hasActiveAccount()) {
            // User already has an account!
            return user.account
        }
        def sub = user.organisation.subscription
        def acc = sub.freeAccount.orElseThrow {throw newBadRequestError(NO_LICENSES)}
        acc.assign(user)
        subscriptionRepository.save(sub)
        return acc
    }

    void unSubscribeEmails(UUID userId) {
        def docutoolsUser = loadUserById(userId)
        docutoolsUser.emailSubscribed = false
        userRepo.save(docutoolsUser)
    }

}
