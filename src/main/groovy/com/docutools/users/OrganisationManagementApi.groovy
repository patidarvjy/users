package com.docutools.users

import com.docutools.exceptions.ErrorCodes
import com.docutools.users.resources.OrganisationDTO
import com.docutools.users.resources.UserDTO
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

import static com.docutools.users.UserManager.currentActor
import static com.docutools.exceptions.ExceptionHelper.*;

/**
 * Provides read and update access for the current users {@link Organisation}.
 */
@RestController
@RequestMapping(path = '/api/v2', produces = 'application/json')
class OrganisationManagementApi {

    static final Logger log = LoggerFactory.getLogger(OrganisationManagementApi)

    @Autowired OrganisationRepo orgRepo
    @Autowired OrganisationManager manager
    @Autowired SessionManager sessionManager

    @ApiOperation(value = "Get Organisation")
    @GetMapping('/organisations/{id}')
    def OrganisationDTO getOrganisation(@PathVariable UUID id) {
        log.info('GET /api/v2/organisation/{} by {}', id, currentActor)

        new OrganisationDTO(orgRepo.findById(id).orElseThrow({newResourceNotFoundError(String.format('Organisation %s', id))}),sessionManager.currentUser)
    }

    @ApiOperation(value = "Update Organisation of an User")
    @PatchMapping('/organisation')
    @PreAuthorize("hasAuthority('admin')")
    def OrganisationDTO updateUsersOrganisation(@RequestBody OrganisationDTO update) {
        log.info('PATCH /api/v2/organisation by {}', currentActor)

        new OrganisationDTO(manager.updateOrganisation(currentOrganisation, update),sessionManager.currentUser)
    }

    @ApiOperation(value = "Get Organisation admins")
    @GetMapping('/organisations/{organisationId}/admins')
    def List<UserDTO> listOrganisationAdmins(@PathVariable UUID organisationId) {
        log.info('GET /api/v2/organisations/{}/admins by {}', organisationId, currentActor)

        manager.listOrganisationsAdmins(organisationId)
    }

    /**
     * Gets the organisation of the currently logged in user.
     *
     * @return current users organisation
     */
    private Organisation getCurrentOrganisation() {
        sessionManager.currentUser.organisation
    }

}
