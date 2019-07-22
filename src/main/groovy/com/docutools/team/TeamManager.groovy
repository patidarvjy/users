package com.docutools.team

import com.docutools.contacts.ProjectContact
import com.docutools.contacts.ProjectContactRepository
import com.docutools.notifications.NotificationCode
import com.docutools.notifications.NotifyClient
import com.docutools.roles.PermissionManager
import com.docutools.roles.Privilege
import com.docutools.roles.Role
import com.docutools.roles.RoleManager
import com.docutools.roles.RoleRepo
import com.docutools.roles.RoleType
import com.docutools.services.core.resources.SortDirection
import com.docutools.services.projects.ProjectApiClient
import com.docutools.services.projects.resources.Project
import com.docutools.users.DocutoolsUser
import com.docutools.users.Organisation
import com.docutools.users.SessionManager
import com.docutools.users.UserManager
import com.docutools.users.UserRepo
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import org.springframework.util.StringUtils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Collectors

import static com.docutools.exceptions.ErrorCodes.CONTACT_NOT_SAME_PROJECT
import static com.docutools.exceptions.ErrorCodes.DIFFERENT_ORGANISATION
import static com.docutools.exceptions.ErrorCodes.MISSING_REQUIRED_VALUE
import static com.docutools.exceptions.ErrorCodes.RESOURCE_NOT_FOUND
import static com.docutools.exceptions.ErrorCodes.USER_NOT_FOUND
import static com.docutools.exceptions.ExceptionHelper.newBadRequestError
import static com.docutools.exceptions.ExceptionHelper.newConflictError
import static com.docutools.exceptions.ExceptionHelper.newForbiddenError
import static com.docutools.exceptions.ExceptionHelper.newInternalServerError
import static com.docutools.exceptions.ExceptionHelper.newResourceNotFoundError
import static com.docutools.exceptions.ExceptionHelper.newUnprivilegedError

/**
 * Handles all CRUD operation around {@link TeamMembership}s.
 */
@Service
@Transactional
class TeamManager {

    private static final Logger log = LoggerFactory.getLogger(TeamManager)

    private static final String USER_ALREADY_MEMBER = 'USER_ALREADY_MEMBER'

    @Autowired
    private UserRepo userRepo
    @Autowired
    private ProjectContactRepository contactRepository
    @Autowired
    private TeamMembershipRepo teamRepo
    @Autowired
    private RoleRepo roleRepo
    @Autowired
    private SessionManager sessionManager
    @Autowired
    private ProjectApiClient projectApi
    @Autowired
    private UserManager userManager
    @Autowired
    private RoleManager roleManager
    @Autowired
    private PermissionManager permissionManager
    @Autowired
    private NotifyClient notifyClient

    /**
     * Checks whether a {@link DocutoolsUser} is member of a project.
     *
     * @param projectId project id
     * @param userId user id
     * @return {@code true} when member.
     */
    boolean isMember(UUID projectId, UUID userId) {
        if(!userRepo.existsById(userId))
            return false
        teamRepo.findMember(userManager.loadUserById(userId), projectId)
                .isPresent()
    }

    /**
     * Adds a new team member to the project.
     *
     * @param projectId id of the project the user will be member in.
     * @param resource the new team member.
     * @return added team member resource.
     */
    TeamMemberDTO addUser(UUID projectId, TeamMemberDTO resource) {
        def project = loadProject(projectId)
        canManageTeam(project)
        Role role = resource.roleIds?.
                collect { loadRole(it, project) }?.
                max {it.roleType.order}
        if(resource.roleId)
            role = loadRole(resource.roleId, project)

        DocutoolsUser invitedUser
        UUID userId = resource.userId
        if (userId) {
            if(userRepo.existsById(userId)) {
                invitedUser = loadUser(userId)
            } else if(contactRepository.existsById(userId)) {
                ProjectContact contact = contactRepository.getOne(userId)
                if(contact.projectId != projectId) {
                    throw newBadRequestError(CONTACT_NOT_SAME_PROJECT)
                }
                if(!contact.email) {
                    throw newBadRequestError(MISSING_REQUIRED_VALUE, "Email")
                }
                invitedUser = userRepo.findByUsernameIgnoreCase(contact.email).orElseGet {
                    def user = userManager.loadOrCreateUser(contact.email, resource.companyId,
                            resource.companyName == null ? contact.getCompanyName() : resource.companyName,
                            projectId, contact.id)
                    if(user.id == contact.id) {
                        user.copyAttributes(contact)
                        userRepo.save(user)
                    }
                    if(role == null) {
                        role = roleManager.getUpdatedOrganisationRoles(project.organisationId).find {it.roleType == RoleType.SubContractor}
                    }
                    return user
                }
                contact.replaceBy(invitedUser)
                contactRepository.save(contact)
            } else {
                throw newBadRequestError(USER_NOT_FOUND)
            }
        } else if (resource.email) {
            invitedUser = userManager.loadOrCreateUser(resource.email, resource.companyId, resource.companyName, projectId)
        } else {
            throw newBadRequestError(MISSING_REQUIRED_VALUE, "UserId or Email")
        }
        if (teamRepo.findMembership(invitedUser.id, projectId).isPresent()) {
            return updateMembership(projectId, invitedUser.id, new TeamMemberDTO(roleId: resource.roleId, roleIds: resource.roleIds, state: resource.state))
        }
        try {
            TeamMembership newMembership = new TeamMembership(invitedUser, projectId, resource.state)
            if(role == null) {
                role = roleManager.getUpdatedOrganisationRoles(project.organisationId).find {it.roleType == RoleType.Viewer}
            }
            newMembership.role = role
            newMembership.lastModified = ZonedDateTime.now()
            newMembership.lastModifiedBy = sessionManager.currentUser.id
            def teamMemberDTO = translate(teamRepo.save(newMembership))
            //Only send project invite notification to already existing user as for new user, we are already sending email while user create.
            if (!invitedUser.isNewCreated) {
                sendUserInviteToProjectNotification(newMembership, project);
            } else {
                invitedUser.invitedBy = sessionManager.currentUser
                userRepo.save(invitedUser)
            }
            return teamMemberDTO
        } catch (DataIntegrityViolationException e) {
            if (e instanceof ConstraintViolationException) {
                throw newConflictError()
            }
            throw e
        }
    }

    private void sendUserInviteToProjectNotification(TeamMembership membership, Project project) {
        notifyClient.builder()
                .code(NotificationCode.USER_INVITE)
                .user(membership.getUser().getId())
                .project(project.getId(), project.getName())
                .build().sendAsync()
    }

    void copyMembers(UUID from, UUID to, List<UUID> members, boolean team) {
        Assert.notNull(from, "from is required - must not be NULL!")
        Assert.notNull(to, "to is required - must not be NULL!")
        if (!permissionManager.hasPrivileges(to, Privilege.ManageTeam)) {
            throw newForbiddenError("ManageTeam")
        }
        if (!permissionManager.hasPrivileges(from, Privilege.ViewTeam)) {
            throw newForbiddenError("ViewTeam")
        }
        if(team) {
            // If team flag is True then copy all `from` Project memberships except the ones in the members list
            teamRepo.findTeam(from).each { teamMembership ->
                if (members == null || members.isEmpty() || !members.contains(teamMembership.user.id)) {
                    def newTeamMember = new TeamMembership(teamMembership.user, to, teamMembership.state)
                    newTeamMember.role = teamMembership.role
                    teamRepo.save(newTeamMember)
                }
            }
        } else {
            if (members != null && !members.isEmpty()) {
                teamRepo.findMembers(members, from).each { teamMembership ->
                    def newTeamMember = new TeamMembership(teamMembership.user, to, teamMembership.state)
                    newTeamMember.role = teamMembership.role
                    teamRepo.save(newTeamMember)
                }
            }
        }
    }

    /**
     * Updates the roles and state of a {@link TeamMembership}.
     *
     * @param projectId id of the project.
     * @param userId id of the user.
     * @param resource the membership resource.
     * @return the updated {@link TeamMemberDTO}
     */
    TeamMemberDTO updateMembership(UUID projectId, UUID userId, TeamMemberDTO resource) {
        def project = loadProject(projectId)
        canManageTeam(project)
        def user = loadUser(userId)
        def member = teamRepo.findMembership(user.id, project.id)
                .orElseThrow { newBadRequestError(USER_NOT_FOUND) }
        if (resource.state) {
            member.state = resource.state
        }
        if (resource.roleIds != null && resource.roleIds.size() > 0) {
            member.role = resource.roleIds
                    .collect { loadRole(it, project) }
                    .max {it.roleType.order}
        }
        if(resource.roleId) {
            member.role = loadRole(resource.roleId, project)
        }
        translate(teamRepo.save(member))
    }

    void bulkUpdateMemberships(UUID userId, Map<UUID, List<UUID>> memberships) {
        memberships.keySet().each {
            if (isMember(it, userId)) {
                if(memberships[it] == null){
                    return
                }
                if(memberships[it].isEmpty()){
                    removeMembership(it, userId)
                } else {
                    updateMembership(it, userId, new TeamMemberDTO(roleIds: memberships[it]))
                }
            } else {
                addUser(it, new TeamMemberDTO(userId: userId, roleIds: memberships[it]))
            }
        }
    }

    /**
     * Removes a {@link DocutoolsUser} from a project.
     *
     * @param projectId project's ID
     * @param userId user's ID
     */
    void removeMembership(UUID projectId, UUID userId) {
        def currentUser = sessionManager.getCurrentUser()
        def user = loadUser(userId)
        def project = loadProject(projectId)
        if(!currentUser.id.equals(userId)){
            canManageTeam(project)
        }
        def member = teamRepo.findMembership(userId, projectId)
                .orElseThrow { newBadRequestError(USER_NOT_FOUND) }
        member.state = MembershipState.Removed
        member.lastModified = ZonedDateTime.now()
        member.lastModifiedBy = sessionManager.currentUser.id
        teamRepo.save(member)
    }

    @Transactional(readOnly = true)
    List<TeamMemberDTO> listMemberships(UUID userId, TeamMemberSort sort, SortDirection sortDirection) {
        listMemberships(loadUser(userId), sort, sortDirection)
    }

    @Transactional(readOnly = true)
    List<TeamMemberDTO> listMemberships(UUID userId, int page, int size,TeamMemberSort sort, SortDirection sortDirection, String search) {
        listMemberships(loadUser(userId), page, size,sort, sortDirection, search)
    }

    Optional<Role> getRoleIn(UUID projectId) {
        teamRepo.findMembership(sessionManager.currentUser.id, projectId)
            .map {it.role}
    }

    /**
     * Lists a users {@link TeamMembership}s for projects in a specified {@link Organisation}.
     *
     * @param user the user.
     * @return a list of the user's memberships in this organisation's projects, including {@link Role}s.
     * @throws com.docutools.apierrors.ApiException when the user has no permission to list this user's memberships.
     * @throws IllegalArgumentException when the user is {@code null}.
     */
    @Transactional(readOnly = true)
    List<TeamMemberDTO> listMemberships(DocutoolsUser user, TeamMemberSort sort, SortDirection sortDirection) {
        Assert.notNull(user, 'TeaManager.listMemberships requires a user.')
        def currentUser = sessionManager.currentUser
        if (user.id != currentUser.id && !currentUser.admin && !currentUser.getSettings().isProjectCreator()) {
            throw newForbiddenError()
        }

        if (!currentUser.admin && !currentUser.getSettings().isProjectCreator()) {
            getProjectIdListFromUserId(user.getId()).stream().map {
                def membership = teamRepo.findMember(user, it)
                        .map { translate(it) }
                        .orElse(translate(user, it))
                return membership
            }.sorted(teamMemberComparator(sort, sortDirection)).collect(Collectors.toList())
        } else {
            return projectApi.getAllProjectsInCurrentOrganisation().stream().map {
                def membership = teamRepo.findMember(user, it.id)
                        .map { translate(it) }
                        .orElse(translate(user, it.id))
                membership.projectName = it.name
                return membership
            }.sorted(teamMemberComparator(sort, sortDirection)).collect(Collectors.toList())
        }
    }

    @Transactional(readOnly = true)
    List<TeamMemberDTO> listMemberships(DocutoolsUser user, int page, int size, TeamMemberSort sort, SortDirection sortDirection, String search) {
        Assert.notNull(user, 'TeamManager.listMemberships requires a user.')
        def currentUser = sessionManager.currentUser
        if (user.id != currentUser.id && !currentUser.admin && !currentUser.getSettings().isProjectCreator()) {
            throw newForbiddenError()
        }

        if (!currentUser.admin && !currentUser.getSettings().isProjectCreator()) {
            return getProjectIdListFromUserId(user.getId(), page, size).content.collect {
                def membership = teamRepo.findMember(user, it)
                        .map { translate(it) }
                        .orElse(translate(user, it))
                return membership
            }
        }
        if (search != null && !search.isEmpty()){
            return projectApi.searchProject(page, size, search).collect {
                def membership = teamRepo.findMember(user, it.id)
                        .map { translate(it) }
                        .orElse(translate(user, it.id))
                membership.projectName = it.name
                return membership
            }
        }

        return projectApi.getAllProjectsInCurrentOrganisation(page, size, Collections.emptyList()).collect {
            def membership = teamRepo.findMember(user, it.id)
                    .map { translate(it) }
                    .orElse(translate(user, it.id))
            membership.projectName = it.name
            return membership
        }
    }

    @Transactional(readOnly = true)
    List<TeamMemberDTO> listAllMemberships(UUID userId) {
        return listAllMemberships(loadUser(userId), false)
    }

    @Transactional(readOnly = true)
    List<TeamMemberDTO> listAllMemberships(DocutoolsUser user, boolean includePermissions = true) {
        Assert.notNull(user, "TeamManager.listmMeberships requires a user.")
        def currentUser = sessionManager.currentUser
        if (user.id != currentUser.id && !currentUser.admin) {
            throw newForbiddenError()
        }

        return teamRepo.findByUser(user)
                .filter { it.state == MembershipState.Active }
                .filter { it.roles.size() > 0 || it.role != null }
                .map { translate(it, includePermissions) }
                .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    List<UUID> getProjectIdListFromUserId(UUID userId){
        Assert.notNull(userId, "TeamManager.listAllMembershipUUIDs requires a user id.")
        def currentUser = sessionManager.currentUser
        if (userId != currentUser.id && !currentUser.admin) {
            throw newForbiddenError('User has no authority to list the user roles.')
        }
        return teamRepo.findProjectIdListFromActiveMembershipsWithRolePresentForUser(userId, "Active")
                .map{uuid -> UUID.fromString(uuid)}
                .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    Page<UUID> getProjectIdListFromUserId(UUID userId, int page, int size){
        Assert.notNull(userId, "TeamManager.listAllMembershipUUIDs requires a user id.")
        def currentUser = sessionManager.currentUser
        if (userId != currentUser.id && !currentUser.admin) {
            throw newForbiddenError('User has no authority to list the user roles.')
        }
        return teamRepo.findProjectIdListFromActiveMembershipsWithRolePresentForUser(userId, "Active", PageRequest.of(page, size))
                .map{uuid -> UUID.fromString(uuid)};
    }

    @Transactional(readOnly = true)
    List<RoleMapDTO> getRoleMapForCurrentUser() {
        def currentUser = sessionManager.currentUser
        return teamRepo.findByUser(currentUser)
            .filter {it.state == MembershipState.Active && (it.roles.size() > 0 || it.role != null)}
            .map {new RoleMapDTO(it)}
            .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    List<UUID> getLeftProjectIdListFromUserId(UUID userId, int page, int size, Instant since) {
        Assert.notNull(userId, "TeamManager.getLeftProjectIdListFromUserId requires a user id.")
        def currentUser = sessionManager.currentUser
        if (userId != currentUser.id && !currentUser.admin) {
            throw newForbiddenError('User has no authority to list the user roles.')
        }

        if (since != null) {
            return teamRepo.findProjectIdListFromRemovedMembershipsSinceWithRolePresentForUser(userId, "Removed", since.atZone(ZoneId.of("UTC")))
                    .map { uuid -> UUID.fromString(uuid) }
                    .skip(page * size)
                    .limit(size)
                    .collect(Collectors.toList())
        } else {
            return teamRepo.findProjectIdListFromActiveMembershipsWithRolePresentForUser(userId, "Removed")
                    .map { uuid -> UUID.fromString(uuid) }
                    .skip(page * size)
                    .limit(size)
                    .collect(Collectors.toList())
        }
    }



    @Transactional(readOnly = true)
    List<TeamMemberDTO> listTeam(UUID projectId,
                                 TeamQuickFilter quickFilter = TeamQuickFilter.All,
                                 StateFilter stateFilter,
                                 TeamMemberSort sort,
                                 SortDirection sortDir) {
        def currentUser = sessionManager.currentUser
        def project = projectApi.getProject(projectId)
        if (currentUser.isUnprivileged() || project.organisationId != currentUser.organisation.id) {
            boolean hasPrivilege = teamRepo.findMember(currentUser, projectId)
                    .map { it.hasPrivilege(Privilege.ViewTeam, Privilege.ManageTeam) }
                    .orElse(false)
            if (!hasPrivilege) {
                throw newUnprivilegedError("ViewTeam or ManageTeam")
            }
        }

        def team = filterTeam(quickFilter, stateFilter, projectId, currentUser)

        Optional<TeamMembership> teamMember = team.stream()
                .filter { membership -> currentUser.id.equals(membership.user.id) }
                .findAny()
        teamMember.ifPresent { it -> team.remove(it); team.add(0, it) }

        sortTeamMembers(team.collect { translate(it) }, sort, sortDir)

    }

    static List<TeamMemberDTO> sortTeamMembers(List<TeamMemberDTO> members, TeamMemberSort sort, SortDirection sortDir){
        members.sort(teamMemberComparator(sort, sortDir))
        return members
    }

    private static Comparator<TeamMemberDTO> teamMemberComparator(TeamMemberSort sort, SortDirection sortDir) {
        if(sort == null){
            sort = TeamMemberSort.Name
        }
        Comparator<TeamMemberDTO> comparator;
        switch (sort) {
            case TeamMemberSort.Name:
                comparator = { m1, m2 -> m1.getName().compareToIgnoreCase(m2.getName()) }
                break
            case TeamMemberSort.ProjectName:
                comparator = { m1, m2 -> m1.getProjectName().compareToIgnoreCase(m2.getProjectName()) }
                break
            case TeamMemberSort.Company:
                comparator = { m1, m2 -> m1.getCompanyName().compareToIgnoreCase(m2.getCompanyName()) }
                break
            case TeamMemberSort.License:
                comparator = { m1, m2 -> m1.getLicenseInfo().getType().compareToIgnoreCase(m2.getLicenseInfo().getType()) }
                break
            case TeamMemberSort.Role:
                comparator = { m1, m2 ->
                    if (!m1.role && !m2.role) {
                        return 0
                    }
                    if (!m1.role) {
                        return 1
                    }
                    if (!m2.role) {
                        return -1
                    }
                    return m2.role.type.order <=> m1.role.type.order
                }
                break
            case TeamMemberSort.Status:
                comparator = { m1, m2 -> m1.getState().compareTo(m2.getState()) }
                break
        }
        if (sortDir == SortDirection.DESC) {
            comparator = comparator.reversed()
        }
        comparator
    }

    /**
     * Adds existing and new {@link DocutoolsUser}s as {@link TeamMembership}s to this project.
     *
     * @param projectId the project's ID.
     * @param bulk the team members in Bulk.
     * @return list of added {@link TeamMembership}s as {@link TeamMemberDTO}
     */
    List<TeamMemberDTO> addMembersInBulk(UUID projectId, TeamMemberBulkDTO bulk) {
        Assert.notNull(bulk)
        log.debug("Adding {} team members to Project <{}>...", bulk.count(), projectId)

        if (!bulk.count()) {
            log.debug("No entries found in TeamMemberBulkDTO, stopping import!")
            return []
        }
        def state = bulk.active ? MembershipState.Active : MembershipState.Inactive
        log.debug("Settings all new team members to state <$state>.")

        def project = projectApi.getProject(projectId)

        def role
        if (!bulk.roleId) {
            role = roleManager.getUpdatedOrganisationRoles(project.organisationId).find {
                it.roleType == RoleType.SubContractor
            }
        } else {
            role = roleRepo.findById(bulk.roleId)
                    .orElseThrow { newResourceNotFoundError('Role', bulk.roleId) }
        }

        if (role.organisation.id != project.organisationId) {
            newForbiddenError()
        }
        log.debug("Granting all new team members the role $role.name (id: $role.id)")

        def addedUsers = bulk.collectUserIds {
            addUser(projectId, new TeamMemberDTO(userId: it, state: state, roleIds: [role.id]))
        }
        log.debug("Added ${addedUsers.size()} existing users to the Project <$projectId>.")

        def invitedUsers = bulk.collectEmails {
            addUser(projectId, new TeamMemberDTO(email: it, state: state, roleIds: [role.id]))
        }
        log.debug("Invited ${invitedUsers.size()} new users to the Project <$projectId>.")

        return addedUsers + invitedUsers
    }

    /**
     * Adds the current user (project creator) to the project as a team member
     * The current user has to be the project creator calling this method
     *
     * @param projectId the project's ID
     * @return added project creator's team member resource
     */
    TeamMemberDTO addCurrentUser(UUID projectId){
        def currentUser = sessionManager.currentUser
        TeamMembership membership = new TeamMembership(currentUser, projectId, MembershipState.Active)
        membership.role = roleManager.getUpdatedOrganisationRoles(currentUser.organisation)
            .find {it.roleType == RoleType.PowerUser}
        return translate(teamRepo.save(membership))
    }

    private List<TeamMembership> filterTeam(TeamQuickFilter quickFilter, StateFilter stateFilter, UUID projectId, DocutoolsUser currentUser) {
        switch (quickFilter) {
            case TeamQuickFilter.All:
                switch (stateFilter) {
                    case StateFilter.Active:
                        return teamRepo.findTeamByState(projectId, Arrays.asList(MembershipState.Active, MembershipState.Invited))
                    case StateFilter.Inactive:
                        return teamRepo.findTeamByState(projectId, Arrays.asList(MembershipState.Inactive, MembershipState.Removed))
                    default:
                        return teamRepo.findByProjectId(projectId).collect(Collectors.toList())
                }
            case TeamQuickFilter.MyCompany:
                switch (stateFilter) {
                    case StateFilter.Active:
                        return teamRepo.filterTeamByMyCompanyAndState(projectId, currentUser.organisation, Arrays.asList(MembershipState.Active, MembershipState.Invited))
                    case StateFilter.Inactive:
                        return teamRepo.filterTeamByMyCompanyAndState(projectId, currentUser.organisation, Arrays.asList(MembershipState.Inactive, MembershipState.Removed))
                    default:
                        return teamRepo.filterTeamByMyCompany(projectId, currentUser.organisation)
                }
            case TeamQuickFilter.OtherCompanies:
                switch (stateFilter) {
                    case StateFilter.Active:
                        return teamRepo.filterByOtherCompaniesAndState(projectId, currentUser.organisation, Arrays.asList(MembershipState.Active, MembershipState.Invited))
                    case StateFilter.Inactive:
                        return teamRepo.filterByOtherCompaniesAndState(projectId, currentUser.organisation, Arrays.asList(MembershipState.Inactive, MembershipState.Removed))
                    default:
                        return teamRepo.filterByOtherCompanies(projectId, currentUser.organisation)
                }
            default:
                throw newInternalServerError("Unknown Team Quick Filter <$quickFilter>!")
        }
    }

    private Project loadProject(UUID projectId) {
        projectApi.getProject(projectId)
    }

    private DocutoolsUser loadUser(UUID userId) {
        userRepo.findById(userId)
                .orElseThrow { newBadRequestError(USER_NOT_FOUND) }
    }

    private void canManageTeam(Project project) {
        def currentUser = sessionManager.currentUser
        if(project.organisationId == currentUser.organisation.id && (currentUser.admin || currentUser.settings.projectCreator))
            return

        def membership = teamRepo.findMember(currentUser, project.id)
                .orElseThrow { newForbiddenError() }
        if (!membership.hasPrivilege(Privilege.ManageTeam)) {
            throw newForbiddenError("ManageTeam")
        }
    }

    private Role loadRole(UUID roleId, Project project) {
        def role = roleRepo.findById(roleId)
                .orElseThrow { newBadRequestError(RESOURCE_NOT_FOUND, "Role") }
        if (role.organisation.id != project.organisationId) {
            throw newBadRequestError(DIFFERENT_ORGANISATION)
        }
        return role
    }

    @Transactional(readOnly = true)
    List<TeamMemberDTO> searchTeamMembers(UUID projectId, String searchText) {
        if (StringUtils.isEmpty(searchText)) {
            return Collections.emptyList()
        }
        def currentUser = sessionManager.currentUser
        def project = projectApi.getProject(projectId)
        if (currentUser.isUnprivileged() || project.organisationId != currentUser.organisation.id) {
            boolean hasPrivilege = teamRepo.findMember(currentUser, projectId)
                    .map { it.hasPrivilege(Privilege.ViewTeam, Privilege.ManageTeam) }
                    .orElse(false)
            if (!hasPrivilege) {
               return Collections.emptyList()
            }
        }
        teamRepo.searchTeamMembers(projectId, searchText).collect { translate(it) }
    }

    @Transactional(readOnly = true)
    Page<TeamMemberDTO> searchTeamMembers(UUID projectId, String searchText, int page, int size) {
        if (StringUtils.isEmpty(searchText)) {
            return new PageImpl<>(Collections.emptyList())
        }
        def currentUser = sessionManager.currentUser
        def project = projectApi.getProject(projectId)
        if (currentUser.isUnprivileged() || project.organisationId != currentUser.organisation.id) {
            boolean hasPrivilege = teamRepo.findMember(currentUser, projectId)
                    .map { it.hasPrivilege(Privilege.ViewTeam, Privilege.ManageTeam) }
                    .orElse(false)
            if (!hasPrivilege) {
                return new PageImpl<>(Collections.emptyList())
            }
        }
        def memberDTOS = teamRepo.searchTeamMembers(projectId, searchText, PageRequest.of(page, size)).collect { translate(it) }
        return new PageImpl<>(memberDTOS)
    }

    @Transactional(readOnly = true)
    TeamMemberDTO getTeamMembership(UUID projectId, UUID userId) {
        Assert.notNull(projectId, "TeamManager.getTeamMembership requires a project id.")
        Assert.notNull(userId, "TeamManager.getTeamMembership requires a user id.")

        Optional<TeamMembership> teamMembership = teamRepo.findMemberAnyState(loadUser(userId), projectId)
        if(teamMembership.isPresent()) {
            return translate(teamMembership.get(), false)
        } else {
            return null
        }
    }

    private TeamMemberDTO translate(TeamMembership membership, boolean includePermissions = true) {
        def dto = new TeamMemberDTO(membership)
        if(includePermissions) {
            def permissions = []
            def currentUser = sessionManager.currentUser
            if (currentUser.hasActiveAccount() && currentUser.isAdmin() && membership.user.organisation.id == currentUser.organisation.id) {
                permissions.add(TeamMemberPermissions.Edit)
            }
            if (permissionManager.hasPrivileges(membership.projectId, Privilege.ManageTeam)) {
                permissions.add(TeamMemberPermissions.Delete)
            }
            dto.permissions = permissions
        }
        return dto
    }

    private TeamMemberDTO translate(DocutoolsUser user, UUID projectId) {
        def permissions = []
        def currentUser = sessionManager.currentUser
        if(currentUser.hasActiveAccount() &&  currentUser.isAdmin() && user.organisation.id == currentUser.organisation.id) {
            permissions.add(TeamMemberPermissions.Edit)
        }
        if(permissionManager.hasPrivileges(projectId, Privilege.ManageTeam)) {
            permissions.add(TeamMemberPermissions.Delete)
        }
        def dto = new TeamMemberDTO(user, projectId)
        dto.permissions = permissions
        return dto
    }

    boolean isUserActiveOrPrivileged(UUID projectId, UUID userId){
        def project = loadProject(projectId)
        def user = loadUser(userId)
        //Check if the user is privileged
        if(project.organisationId.equals(user.organisation.id) && user.isPrivileged()) {
            return true
        }
        //Get the user as team member
        def member = teamRepo.findMember(user, project.id)
        if(!member.isPresent() || !member.get().hasPrivilege(Privilege.ViewTasks)){
            return false
        }
        return member.get().state.equals(MembershipState.Active)
    }
}
