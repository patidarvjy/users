package com.docutools.team

import com.docutools.config.api.BaseController
import com.docutools.roles.RoleDTO
import com.docutools.services.core.resources.SortDirection
import com.docutools.users.UserManager
import com.docutools.users.resources.UserDTO
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import static com.docutools.exceptions.ExceptionHelper.newInputValidationError
import static com.docutools.utils.DateUtils.parseDateTime

@RestController
@RequestMapping(value = '/api/v2', produces = 'application/json')
class MembershipsApi extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(MembershipsApi)

    @Autowired
    private TeamManager teamManager

    @Autowired
    private UserManager userManager

    @ApiOperation(value = "Get Project User Matrix")
    @GetMapping(value = '/users/{userId}/projects')
    List<TeamMemberDTO> getProjectUserMatrix(@PathVariable UUID userId,
                                             @RequestParam(required = false, defaultValue = 'ProjectName') TeamMemberSort sort,
                                             @RequestParam(required = false, defaultValue = 'ASC') SortDirection sortDir) {
        log.info('GET /api/v2/users/{}/projects?sort={}&sortDir={} by {}', userId, sort, sortDir, userName)

        teamManager.listMemberships(userId, sort, sortDir)
    }

    @ApiOperation(value = "Get Project User Matrix Paged")
    @GetMapping(value = '/users/{userId}/projects/paged')
    List<TeamMemberDTO> getProjectUserMatrix(@PathVariable UUID userId,
                                             @RequestParam(required = false, defaultValue = "0") int page,
                                             @RequestParam(required = false, defaultValue = "10") int size,
                                             @RequestParam(required = false, defaultValue = 'ProjectName') TeamMemberSort sort,
                                             @RequestParam(required = false, defaultValue = 'ASC') SortDirection sortDir,
                                             @RequestParam(required = false, defaultValue = '') String search) {
        log.info('GET /api/v2/users/{}/projects/paged?page={}&size={}&sort={}&sortDir={} by {}', userId, page, size, sort, sortDir, userName)

        teamManager.listMemberships(userId, page, size, sort, sortDir, search)
    }

    @ApiOperation(value = "Get User Role for Project")
    @GetMapping('/me/role')
    HttpEntity<RoleDTO> getRoleFor(@RequestParam UUID projectId) {
        log.debug('GET /api/v2/me/role?projectId={}', projectId)
        return teamManager.getRoleIn(projectId)
            .map {new RoleDTO(it)}
            .map {new HttpEntity<>(it)}
            .orElseGet {new ResponseEntity<>(HttpStatus.NOT_FOUND)}
    }

    @ApiOperation(value = "Get Users Membership in all Projects")
    @GetMapping(path = "/users/{userId}/projects/all")
    List<TeamMemberDTO> getUsersMemberships(@PathVariable UUID userId) {
        log.info('GET /api/v2/users/{}/projects/all by {}', userId, userName)
        return teamManager.listAllMemberships(userId)
    }

    @GetMapping(path = "/users/{userId}/projects/all/projectId")
    List<UUID> getUsersMembershipProjectIds(@PathVariable UUID userId) {
        log.info('GET /api/v2/users/{}/projects/all/projectId by {}', userId, userName)
        return teamManager.getProjectIdListFromUserId(userId)
    }


    @GetMapping(path = "/users/{userId}/projects/all/projectId/paged")
    Page<UUID> getUsersMembershipProjectIdsPaged(@PathVariable UUID userId,
                                                 @RequestParam(defaultValue = '0') int page,
                                                 @RequestParam(defaultValue = '10') int size) {
        log.info('GET /api/v2/users/{}/projects/all/projectId/paged by {}', userId, userName)
        return teamManager.getProjectIdListFromUserId(userId, page, size)
    }

    @GetMapping(path = "/me/memberships/all/roleMap")
    List<RoleMapDTO> getUsersMembershipRoleMap() {
        log.info('GET /api/v2/me/memberships/all/roleMap by {}', userName)
        return teamManager.getRoleMapForCurrentUser()
    }

    @GetMapping(path = "/users/{userId}/projects/removed/projectId")
    List<UUID> getLeftUsersMembershipProjectIds(@PathVariable UUID userId,
                                                @RequestParam(required = false, defaultValue = "0") int page,
                                                @RequestParam(required = false, defaultValue = "10") int size,
                                                @RequestParam(required = false) String since) {
        log.info('GET /api/v2/users/{}/projects/all by {}', userId, userName)
        return teamManager.getLeftProjectIdListFromUserId(userId, page, size, parseDateTime(since))
    }

    @ApiOperation(value = "Get the Team of a Project")
    @GetMapping(value = '/projects/{projectId}/team')
    List<TeamMemberDTO> getTeam(@PathVariable UUID projectId,
                                @RequestParam(required = false, defaultValue = 'Name') TeamMemberSort sort,
                                @RequestParam(required = false, defaultValue = 'ASC') SortDirection sortDir,
                                @RequestParam(required = false) StateFilter stateFilter,
                                @RequestParam(required = false, defaultValue = 'All') TeamQuickFilter quickFilter) {
        log.info('GET /api/v2/projects/{}/team?filter={}&stateFilter={} by {}', projectId, quickFilter, stateFilter, userName)

        teamManager.listTeam(projectId, quickFilter, stateFilter, sort, sortDir)
    }

    @ApiOperation(value = "Create a Member in a Team", notes = "Required Attributes: userId")
    @PostMapping(value = '/projects/{projectId}/team')
    HttpEntity<TeamMemberDTO> createMember(@PathVariable UUID projectId,
                                           @RequestBody TeamMemberDTO resource) {
        log.info('POST /api/v2/projects/{}/team by {}', projectId, userName)

        if (!resource.userId) {
            throw newInputValidationError('Cannot create new team member without user Id.')
        }
        def userId = resource.userId

        if (teamManager.isMember(projectId, userId)) {
            new ResponseEntity<>(teamManager.updateMembership(projectId, userId, resource), HttpStatus.OK)
        } else {
            new ResponseEntity<>(teamManager.addUser(projectId, resource), HttpStatus.CREATED)
        }
    }

    @ApiOperation(value = "Copy Member from a Project Team")
    @PutMapping("/projects/{projectId}/team")
    void copyMembers(@PathVariable UUID projectId,
                     @RequestParam UUID from,
                     @RequestBody(required = false) List<UUID> members,
                     @RequestParam(name = "team", required = false, defaultValue = "true") boolean team) {
        log.debug('PUT /api/v2/projects/{}/team from={}', projectId, from)
        teamManager.copyMembers(from, projectId, members, team)
    }

    /**
     * This endpoint adds the current user (project creator) to the team with a standard role
     */
    @ApiOperation(value = "Add the current User to the Project Team")
    @PutMapping(value = 'projects/{projectId}/team/creator')
    @PreAuthorize("hasAnyAuthority('admin', 'project_creator')")
    HttpEntity<TeamMemberDTO> addCurrentUserToTeam(@PathVariable UUID projectId){
        log.info('POST /api/v2/projects/{}/team/creator', projectId)

        new ResponseEntity<>(teamManager.addCurrentUser(projectId), HttpStatus.OK)
    }

    @ApiOperation(value = "Update Member in a Team", notes = "Required Attributes: none")
    @PatchMapping(value = '/projects/{projectId}/team/{userId}', consumes = 'application/json')
    ResponseEntity<TeamMemberDTO> updateMember(@PathVariable UUID projectId,
                                               @PathVariable UUID userId,
                                               @RequestBody TeamMemberDTO resource) {
        log.info('PATCH /api/v2/projects/{}/team/{} by {}', projectId, userId, userName)

        if (teamManager.isMember(projectId, userId)) {
            new ResponseEntity<>(teamManager.updateMembership(projectId, userId, resource), HttpStatus.OK)
        } else {
            resource.userId = userId
            new ResponseEntity<>(teamManager.addUser(projectId, resource), HttpStatus.CREATED)
        }
    }

    @ApiOperation(value = "Remove a User from a Team")
    @DeleteMapping(value = '/projects/{projectId}/team/{userId}')
    void removeMember(@PathVariable UUID projectId,
                      @PathVariable UUID userId) {
        log.info("DELETE /api/v2/projects/{}/team/{} by {}", projectId, userId, userName)

        teamManager.removeMembership(projectId, userId)
    }

    @ApiOperation(value = "Bulk Update Memberships")
    @PatchMapping(value = '/users/{userId}/memberships', consumes = 'application/json')
    void bulkUpdateMemberships(@PathVariable UUID userId,
                               @RequestBody Map<UUID, List<UUID>> projects) {
        log.info('PATCH /api/v2/users/{}/memberships by {}', userId, userName)
        log.debug('Request Body: {}', projects)

        teamManager.bulkUpdateMemberships(userId, projects)
    }

    @ApiOperation(value = "Add Members in Bulk", notes = "Required Attributes: none")
    @PostMapping(value = '/projects/{projectId}/team/many', consumes = 'application/json')
    List<TeamMemberDTO> addMembersInBulk(@PathVariable UUID projectId, @RequestBody TeamMemberBulkDTO bulk) {
        log.info('POST /api/v2/projects/{}/team/many by {}', projectId, userName)
        log.debug('Request body: {}', bulk)

        teamManager.addMembersInBulk(projectId, bulk)
    }

    @ApiOperation(value = "Search Team")
    @GetMapping(value = '/team/search')
    List<TeamMemberDTO> searchTeam(@RequestParam(name = "search") String searchText,
                                   @RequestParam(name = "for") UUID projectId) {
        log.info('GET /api/v2/team/search?search={}&for={} by {}', searchText, projectId, userName)

        teamManager.searchTeamMembers(projectId, searchText)
    }

    @ApiOperation(value = "Search Team (Paged)")
    @GetMapping(value = '/team/search/paged')
    Page<TeamMemberDTO> searchTeam(@RequestParam(name = "search") String searchText,
                                   @RequestParam(name = "for") UUID projectId,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info('GET /api/v2/team/paged/search?search={}&for={}&page=0&size=10')
        teamManager.searchTeamMembers(projectId, searchText, page, size)
    }

    @ApiOperation(value = "Get Users with other Organisations")
    @GetMapping('/members')
    Page<UserDTO> listUsersWithOtherOrganizationUsers(
            @RequestParam(defaultValue = '0') int page,
            @RequestParam(defaultValue = '10') int pageSize,
            @RequestParam(defaultValue = '') String search) {
        log.info('GET /api/v2/members?page={}&pageSize={}&search={}',page,pageSize,search)
        userManager.listUsersWithinAllOrg(page, pageSize, search)
                .map({ new UserDTO(it) })
    }

    @ApiOperation(value = "Get user membership for project")
    @GetMapping(value = '/users/{userId}/project/{projectId}/member')
    TeamMemberDTO getProjectUserMembership(@PathVariable UUID userId,
                                           @PathVariable UUID projectId) {
        log.info("GET /users/{userId}/project/{projectId}/member")
        return teamManager.getTeamMembership(projectId, userId)
    }
}
