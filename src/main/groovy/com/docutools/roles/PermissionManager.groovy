package com.docutools.roles

import com.docutools.services.internal.InternalApiClient
import com.docutools.team.TeamMembership
import com.docutools.team.TeamMembershipRepo
import com.docutools.users.DocutoolsUser
import com.docutools.users.SessionManager
import com.docutools.users.UserRepo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.stream.Collectors

import static com.docutools.exceptions.ExceptionHelper.newResourceNotFoundError

/**
 * Service methods for checking the current user's permissions.
 */
@Service
@Transactional(readOnly = true)
class PermissionManager {

    @Autowired
    private SessionManager sessionManager
    @Autowired
    private TeamMembershipRepo membershipRepo
    @Autowired
    private UserRepo userRepo
    @Autowired
    private InternalApiClient internalApiClient

    private static final Logger log = LoggerFactory.getLogger(PermissionManager)


    boolean hasPrivileges(UUID projectId, Privilege...privileges) {
        return checkPrivilege(projectId, privileges.toList(), false).check
    }

    boolean hasAnyPrivileges(UUID projectId, Privilege...privileges) {
        return checkPrivilege(projectId, privileges.toList(), true).check
    }

    /**
     * Checks whether the user has any or all of the supplied privileges in the referenced project.
     *
     * @param projectId the project's id
     * @param privileges array of privileges
     * @param any if the user has to have any or all of these privileges
     * @return {@link PrivilegeCheckDTO}
     */
    PrivilegeCheckDTO checkPrivilege(UUID projectId, List<Privilege> privileges, boolean any = false) {
        def user = sessionManager.currentUser
        def project = internalApiClient.getProjectById(projectId).get()
        // TODO had to remove call to isProjectAdminForProject since project creators could not invite anymore
        if ((user.settings.admin || user.organisationOwner || user.settings.projectCreator)
                && user.organisation.id == project.organisationId) {
            def filteredPrivileges = filterByAccount(privileges, user, null)
            def check = any ? filteredPrivileges.size() > 0 : filteredPrivileges.size() == privileges.size()
            return new PrivilegeCheckDTO(privileges: filteredPrivileges, currentUserId: user.id, projectId: projectId, check: check, any: any)
        }
        def response = membershipRepo.findMember(user, projectId)
        if (response.present) {
            def membership = response.get()
            def granted = privileges.findAll { membership.hasPrivilege(it) }
            def filteredPrivileges = filterByAccount(granted, user, membership)
            def check = any ? filteredPrivileges.size() > 0 : filteredPrivileges.size() == privileges.size()
            return new PrivilegeCheckDTO(privileges: filteredPrivileges, currentUserId: user.id, projectId: projectId, check: check, any: any)
        } else {
            return new PrivilegeCheckDTO(privileges: [], currentUserId: user.id, projectId: projectId, check: false, any: any)
        }
    }

    private boolean isProjectAdminForProject(DocutoolsUser user, UUID projectId) {
        if (user.settings.projectCreator) {
            try {
                return internalApiClient.getProjectById(projectId).get()
            } catch (Exception e) {
                log.warn('Error in checking project is inside restricted folder project Id : {}, user id :{}', projectId, user.id, e)
                return false
            }
        }
        return false
    }

    /**
     * Checks whether the user has an active account (subscription) and returns the given privileges or privileges of a
     * free user. Free user have the same privileges as the subcontractor role.
     *
     * @param privileges the users privileges
     * @param user the user
     * @return the list of privileges granted to the user
     */
    private List<Privilege> filterByAccount(List<Privilege> privileges, DocutoolsUser user, TeamMembership membership) {
        if(membership?.role?.roleType == RoleType.Viewer){
            return privileges
        }
        if (user.hasActiveAccount()) {
            return privileges
        }
        return new ArrayList<>(
                DefaultRoles.instance.getPrivilegesForRoleType(RoleType.SubContractor)
                .orElse( [] ).stream().filter{privilege-> privileges.contains(privilege)}.collect(Collectors.toSet()))
    }

    /**
     * Checks whether the current user is an active member of a project (meaning has a role in this project, or is
     * administrator in the projects organisation.
     *
     * @param projectId ID of the project.
     * @return {@code true} when is member
     */
    boolean isMember(UUID projectId) {
        def user = sessionManager.currentUser
        def hasRole = membershipRepo.findMember(user, projectId)
            .map({true})
            .orElse(false)
        if(hasRole) {
            return true
        } else if(user.settings.admin || user.settings.projectCreator) {
            def project = internalApiClient.getProjectById(projectId).get()
            if(project.getOrganisationId().equals(user.getOrganisation().getId())){
                return true
            }
        }
        return false
    }


    PrivilegeCheckDTO checkPrivilegeInternal(UUID userId, UUID projectId, List<Privilege> privileges, boolean any) {
        def optionalUser = userRepo.findById(userId)
        if (!optionalUser.isPresent()) {
            return new PrivilegeCheckDTO(privileges: [], currentUserId: userId, projectId: projectId, check: false, any: any)
        }
        def optionalProject = internalApiClient.getProjectById(projectId);
        if (!optionalProject.isPresent()) {
            return new PrivilegeCheckDTO(privileges: [], currentUserId: userId, projectId: projectId, check: false, any: any)
        }
        def user = optionalUser.get()
        def orgId = optionalProject.get().getOrganisationId()
        if ((user.settings.admin || user.organisationOwner || user.settings.projectCreator)
                && user.organisation.id == orgId) {
            def filteredPrivileges = filterByAccount(privileges, user, null)
            def check = any ? filteredPrivileges.size() > 0 : filteredPrivileges.size() == privileges.size()
            return new PrivilegeCheckDTO(privileges: filteredPrivileges, currentUserId: user.id, projectId: projectId, check: check, any: any)
        }
        def response = membershipRepo.findMember(user, projectId)
        if (response.present) {
            def membership = response.get()
            def granted = privileges.findAll { membership.hasPrivilege(it) }
            def filteredPrivileges = filterByAccount(granted, user, membership)
            def check = any ? filteredPrivileges.size() > 0 : filteredPrivileges.size() == privileges.size()
            return new PrivilegeCheckDTO(privileges: filteredPrivileges, currentUserId: user.id, projectId: projectId, check: check, any: any)
        } else {
            return new PrivilegeCheckDTO(privileges: [], currentUserId: user.id, projectId: projectId, check: false, any: any)
        }
    }

    /**
     * This method is for internal api to check user is member of project or not.
     * Note: Not considering admin and project creators
     * @param projectId
     * @return
     */
    boolean isMembershipExist(UUID userId, UUID projectId) {
        def user = userRepo.findById(userId).orElseThrow { newResourceNotFoundError("User", userId) }
        return membershipRepo.findMember(user, projectId)
                .map({ true })
                .orElse(false)
    }
}
