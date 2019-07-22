package com.docutools.users

import com.docutools.users.resources.NotificationDTO
import com.docutools.users.resources.UserBatchUpdate
import com.docutools.users.resources.UserDTO
import com.docutools.users.resources.UserFilter
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import static UserManager.currentActor
import static com.docutools.exceptions.ExceptionHelper.*;
import static org.springframework.http.HttpStatus.CREATED

/**
 * Requests for organisation admins to manage their organisation's users.
 */
@RestController
@RequestMapping(path = '/api/v2/users', produces = 'application/json')
class UserManagementApi {

    static final Logger log = LoggerFactory.getLogger(UserManagementApi)

    @Autowired
    UserManager manager
    @Autowired
    SessionManager sessionManager

    @ApiOperation(value = "Get User")
    @GetMapping(path = '/{id}')
    //@PreAuthorize("hasAnyAuthority('admin', 'project_creator')")
    UserDTO getUser(@PathVariable UUID id) {
        log.info('GET /api/v2/users/{} by {}', id, currentActor)

        new UserDTO(manager.loadUserById(id))
    }

    @ApiOperation(value = "Get User By Email")
    @GetMapping(path = "/byEmail")
    UserDTO getUserByEmail(@RequestParam String email) {
        log.info('GET /api/v2/users/byEmail')

        new UserDTO(manager.getUserByEmail(email))
    }

    @ApiOperation(value = "Invite New User", notes = "Required Attributes: email")
    @PostMapping(consumes = 'application/json')
    @PreAuthorize("hasAuthority('admin')")
    @ResponseStatus(CREATED)
    UserDTO inviteNewUser(@RequestBody UserDTO body) {
        log.info('POST /api/v2/users by {}', currentActor)

        if (!body.email) {
            throw newInputValidationError('You have to specify an emaill address to invite a new user.')
        }

        new UserDTO(manager.inviteNewUser(body, sessionManager.currentUser.organisation))
    }

    @ApiOperation(value = "Update User", notes = "Required Attributes: none")
    @PatchMapping(value = '/{userId}', consumes = 'application/json')
    @PreAuthorize("hasAuthority('admin')")
    UserDTO updateUser(@PathVariable UUID userId,
                       @RequestBody UserDTO body) {
        log.info('PATCH /api/v2/users/{} by {}', userId, currentActor)

        new UserDTO(manager.updateUser(manager.loadUserById(userId), body))
    }

    @ApiOperation(value = "Update multiple Users", notes = "Required Attributes: ids, update")
    @PatchMapping(consumes = 'application/json')
    @PreAuthorize("hasAnyAuthority('admin', 'project_creator')")
    List<UserDTO> updateAll(@RequestBody UserBatchUpdate body) {
        log.info('PATCH /api/v2/users by {}', currentActor)

        manager.updateAll(body.ids, body.update)
                .collect { new UserDTO(it) }
    }

    @ApiOperation(value = "Get Users paged")
    @GetMapping
    Page<UserDTO> listUsers(@RequestParam(defaultValue = '0', required = false) int page,
                            @RequestParam(required = false) Integer pageSize,
                            @RequestParam(defaultValue = '10', required = false) int size,
                            @RequestParam(defaultValue = 'Any', required = false) UserFilter filter,
                            @RequestParam(defaultValue = 'name', required = false) String sort,
                            @RequestParam(defaultValue = 'ASC', required = false) Sort.Direction sortDirection,
                            @RequestParam(defaultValue = '', required = false) String search) {
        manager.listOrganisationUsers(page, pageSize != null ? pageSize : size, sort, sortDirection, filter, search)
                .map({ new UserDTO(it) })
    }


    @ApiOperation(value = "Export a User as a VCard")
    @GetMapping(value = '/{id}/vcard', produces = 'text/x-vcard')
    @PreAuthorize("hasAnyAuthority('admin', 'project_creator')")
    String downloadVCard(@PathVariable UUID id) {
        log.info('GET /api/v2/users/{}/vcard by {}', id, currentActor)

        manager.exportVCard(manager.loadUserById(id))
    }


    @ApiOperation(hidden = true, value = "Notify User on Task Asigned")
    @PostMapping(value = '/{id}/taskAssigned')
    void notifyUserOnTaskAssigned(@PathVariable UUID id,
                                    @RequestBody NotificationDTO body){
        log.info('GET /api/v2/users/{}/taskAssigned', id)

        manager.notifyTaskAssigned(manager.loadUserById(id), body)
    }

    @ApiOperation(value = "Reinvite User")
    @PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping(value = '/{id}/reInvite')
    void reInviteUser(@PathVariable UUID id){
        log.info('GET /api/v2/users/{}/reInvite', id)
        manager.reSendInvitationEmail(id)
    }

    @ApiOperation(value = "Remove License")
    @PreAuthorize("hasAnyAuthority('admin')")
    @DeleteMapping("/{id}/license")
    void removeLicense(@PathVariable UUID id) {
        log.debug("DELETE /api/v2/users/$id/license")
        manager.removeLicense(manager.loadUserById(id))
    }

    @ApiOperation(value = "Assign License")
    @PreAuthorize("hasAnyAuthority('admin')")
    @PostMapping("/{id}/license")
    void assignLicense(@PathVariable UUID id) {
        log.debug("POST /api/v2/users/$id/license")
        def user = manager.loadUserById(id)
        manager.assignLicense(user)
    }

    @ApiOperation(value = "Unsubscribe from Emails")
    @GetMapping("/unsubscribe")
    void unSubscribeEmails(@RequestParam UUID userId) {
        log.debug("GET /api/v2/users/unsubscribe?userId=$userId")
        manager.unSubscribeEmails(userId)
    }
}
