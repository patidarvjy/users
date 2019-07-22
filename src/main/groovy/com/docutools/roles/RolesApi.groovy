package com.docutools.roles

import com.docutools.exceptions.ExceptionHelper
import com.docutools.exceptions.ErrorCodes
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import java.util.stream.Collectors

import static com.docutools.users.UserManager.currentActor

@RestController
@RequestMapping(value = '/api/v2', produces = 'application/json')
class RolesApi {

    static final Logger log = LoggerFactory.getLogger(RolesApi)

    @Autowired
    private RoleManager roleManager

    @ApiOperation(value = "Get all possible Privileges")
    @GetMapping(value = '/privileges')
    List<String> getAllPrivileges() {
        log.info('GET /api/v2/privileges by {}', currentActor)

        Arrays.stream(Privilege.values())
            .map({it.toString()})
            .collect(Collectors.toList())
    }

    @ApiOperation(value = "List the Roles of the current User in your organisation")
    @GetMapping(value = '/roles')
    List<RoleDTO> listRolesInUsersOrganisation(@RequestParam(required = false, defaultValue = 'name') String sort,
                                               @RequestParam(required = false, defaultValue = 'ASC') Sort.Direction sortDirection) {
        log.info('GET /api/v2/roles?sort={}&sortDirection={} by {}', sort, sortDirection, currentActor)

        roleManager.listRoles(sort, sortDirection)
    }

    @ApiOperation(value = "List the Roles of the current User in a Organisation")
    @GetMapping(value = '/organisations/{organisationId}/roles')
    List<RoleDTO> listRolesInOrganisation(@PathVariable UUID organisationId,
                                          @RequestParam(required = false, defaultValue = 'name') String sort,
                                          @RequestParam(required = false, defaultValue = 'ASC') Sort.Direction sortDirection) {
        log.info('GET /api/v2/organisations/{}/roles?sort={}&sortDirection={} by {}',
            organisationId, sort, sortDirection, currentActor)

        roleManager.listRoles(organisationId, sort, sortDirection)
    }

    @ApiOperation(value = "Create a Role (Disabled)")
    @PostMapping(value = '/roles', consumes = 'application/json')
    @PreAuthorize("hasAnyAuthority('admin', 'projectCreator')")
    @ResponseStatus(HttpStatus.CREATED)
    RoleDTO createRole(@RequestBody RoleDTO role) {
        log.info('POST /api/v2/roles by {}', currentActor)

        // Disable the creation of (custom) roles as the user should not be able to create those
        throw ExceptionHelper.newBadRequestError(ErrorCodes.ENDPOINT_DISABLED)
        // roleManager.create(role.name, role.privileges)
    }

    @ApiOperation(value = "Update a Role", notes = "Required Attributes: none")
    @PatchMapping(value = '/roles/{roleId}', consumes = 'application/json')
    @PreAuthorize("hasAnyAuthority('admin', 'projectCreator')")
    RoleDTO updateRole(@PathVariable UUID roleId,
                       @RequestBody RoleDTO role) {
        log.info('PATCH /api/v2/roles/{} by {}', roleId, currentActor)

        roleManager.update(roleId, role)
    }

    @ApiOperation(value = "Get all possible Roles with privileges")
    @GetMapping(value = '/roles/default')
    List<DefaultRoleDTO> getAllRolesWithPrivileges() {
        roleManager.getDefaultRoles()
                .stream()
                .map({ new DefaultRoleDTO(it) })
                .collect(Collectors.toList())
    }

}
