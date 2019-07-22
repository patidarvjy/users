package com.docutools.roles

import com.docutools.exceptions.ErrorCodes
import com.docutools.users.SessionManager
import com.docutools.users.Organisation
import com.docutools.users.OrganisationRepo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert

import java.util.stream.Collectors

import static com.docutools.exceptions.ExceptionHelper.*

/**
 * Manages creation, update and retrieval of roles.
 */
@Service
@Transactional
class RoleManager {

    private static final Logger log = LoggerFactory.getLogger(RoleManager)

    private static final String[] allowedRoleSortProperties = ['name', 'lastModified', 'createdBy', 'activeProjects']

    @Autowired
    private RoleRepo roleRepo
    @Autowired
    private SessionManager sessionManager
    @Autowired
    private OrganisationRepo organisationRepo

    private final DefaultRoles defaultRoles = DefaultRoles.instance

    /**
     * Creates a new role for the current user's organisation.
     *
     * @param name name of the role, max 64 characters.
     * @param privileges list of privileges for this role.
     * @return the new {@link RoleDTO}.
     * @throws com.docutools.apierrors.ApiException when there was an input validation error or the user is not
     * allowed to create such a role.
     */
    RoleDTO create(String name, Set<Privilege> privileges) {
        def currentUser = sessionManager.currentUser
        if(!currentUser.isAdmin()) {
            throw newForbiddenError('Only organisation owner or admins can create roles.')
        }
        Role newRole = roleRepo.save(new Role(name, privileges, currentUser.organisation, currentUser))
        return new RoleDTO(newRole)
    }

    /**
     * Updates a role's name and/or privileges.
     *
     * @param roleId id of the role.
     * @param update updates for this role.
     * @return updated {@link RoleDTO}
     * @throws com.docutools.apierrors.ApiException when there was an input validation error, no role with that id exists or the user
     * is not allowed to updated roles in that organisation.
     */
    RoleDTO update(UUID roleId, RoleDTO update) {
        def role = roleRepo.findById(roleId)
            .orElseThrow {newBadRequestError(ErrorCodes.RESOURCE_NOT_FOUND, "role")}
        if(!role) {
            throw newResourceNotFoundError("role")
        }
        def currentUser = sessionManager.currentUser
        if(currentUser.organisation != role.organisation) {
            throw newForbiddenError('The user is not allowed to update roles in this organisation.')
        }
        if(!currentUser.settings.admin) {
            throw newForbiddenError('Only organisation owner or admins can update roles.')
        }
        if(update.name) role.name = update.name
        if(update.privileges) role.privileges = update.privileges
        if(update.active != null && role.active != update.active) {
            log.debug('{} role {} (id: {})', update.active? 'Activated' : 'Deactivated', role.name, role.id)
            role.active = update.active
        }
        def updatedRole = roleRepo.save(role)
        return new RoleDTO(updatedRole)
    }

    /**
     * Listing all roles in the current user's {@link Organisation}, {@link RoleManager#listRoles(Organisation,String,Sort.Direction)}.
     *
     * @param sort
     * @param sortDirection
     * @return
     */
    @Transactional
    List<RoleDTO> listRoles(String sort = 'lastModified', Sort.Direction sortDirection = Sort.Direction.DESC) {
        def currentUser = sessionManager.currentUser
        return listRoles(currentUser.organisation, sort, sortDirection)
    }

    @Transactional
    List<RoleDTO> listRoles(UUID organisationId, String sort = 'lastModified', Sort.Direction sortDirection = Sort.Direction.DESC) {
        def organisation = organisationRepo.findById(organisationId)
            .orElseThrow {newResourceNotFoundError('Organisation', organisationId)}
        return listRoles(organisation, sort, sortDirection)
    }

    /**
     * Lists the roles of this organisation.
     *
     * @param organisation the {@link Organisation}
     * @param sort property to sort after (either name, lastModified, createdBy, activeProjects).
     * @param sortDirection to sort either ascending or descending.
     * @return the list of sorted {@link RoleDTO}s.
     */
    @Transactional
    List<RoleDTO> listRoles(Organisation organisation, String sort = 'lastModified', Sort.Direction sortDirection = Sort.Direction.DESC) {
        Assert.notNull(organisation, 'Cannot list roles of null organisations.')
        Assert.notNull(sortDirection, 'Cannot sort after null direction.')

        if(!allowedRoleSortProperties.contains(sort)) {
            throw newInputValidationError("Unknown sort property for roles <$sort>, allowed properties " +
                    "are: $allowedRoleSortProperties!")
        }

        // When sort by active projects is selected, first sort by title from the database
        String sortProperty = sort
        boolean sortByActiveProjects = 'activeProjects' == sort
        if(sortByActiveProjects) {
            sortProperty = 'name'
        }

        // Sort after the creators name instead of foreign key
        if(sort == 'createdBy') {
            sortProperty = 'createdBy.name'
        }

        def roles = getUpdatedOrganisationRoles(organisation, sortProperty, sortDirection).stream()
            .map {new RoleDTO(it, countActiveProjects(it.id))}
            .collect(Collectors.toList())

        // When sort by active projects is selected, sorting has to be done manually
        if(sortByActiveProjects) {
            roles.toSorted {role1, role2 ->
                if(sortDirection == Sort.Direction.ASC)
                    role1.activeProjects - role2.activeProjects
                else
                    role2.activeProjects - role1.activeProjects
            }
        } else
            return roles
    }

    List<Role> getUpdatedOrganisationRoles(UUID organisation, String sort = 'lastModified', Sort.Direction sortDirection = Sort.Direction.DESC){
        getUpdatedOrganisationRoles(organisationRepo.getOne(organisation), sort, sortDirection)
    }

    List<Role> getUpdatedOrganisationRoles(Organisation organisation, String sort = 'lastModified', Sort.Direction sortDirection = Sort.Direction.DESC){
        def roles = roleRepo.findByOrganisation(organisation, new Sort(sortDirection, sort))
        if(roles.size() == 0){
            return defineDefaultRoles(organisation)
        } else {
            // Get current roles
            Set<RoleType> currentRoles = roles.stream()
                    .filter { line -> line.isDefaultRole() }
                    .map{r -> r.getRoleType()}
                    .collect(Collectors.toSet())
            // Get default roles that werent in the current roles
            Set<RoleType> newRoles = defaultRoles.getActiveDefaultRoleTypes().stream()
                    .filter{ role -> !currentRoles.contains(role)}
                    .collect(Collectors.toSet())
            getNewDefaultRoles(organisation, newRoles).ifPresent{collection -> roles.addAll(collection)}
            return roles
        }
    }

    private Collection<Role> defineDefaultRoles(Organisation organisation) {
        List<Role> roles = defaultRoles.getActiveDefaultRoleTypes().stream()
                .map {r -> getDefaultRole(organisation, r)}
                .filter {optional -> optional.isPresent()}
                .map {optional -> optional.get()}
                .filter {role -> role.active}
                .collect(Collectors.toList())

        roleRepo.saveAll(roles)
    }

    private Optional<Collection<Role>> getNewDefaultRoles(Organisation organisation, Set<RoleType> newRoles){
        if(newRoles.empty){
            return Optional.empty();
        }
        List<Role> newRoleList = newRoles.stream()
                .map{role -> getDefaultRole(organisation, role)}
                .filter {optional -> optional.isPresent()}
                .map {optional -> optional.get()}
                .filter {role -> role.active}
                .collect(Collectors.toList())
        Optional.of(roleRepo.saveAll(newRoleList))
    }

    private Optional<Role> getDefaultRole(Organisation organisation, RoleType roleType){
        def data = defaultRoles.roleData
        if(data.empty){
            return Optional.empty()
        }
        List<Optional<Role>> roles = data.stream()
                .filter{roledata -> roledata.getRoleType().equals(roleType)}
                .map{roledata -> roledata.createRole(organisation)}
                .filter {role -> role.active}
                .map{role -> Optional.ofNullable(role)}
                .filter{optional -> optional.isPresent()}
                .collect(Collectors.toList())
        if(roles.empty){
            log.warn("Can not find role of roletype: %s!", roleType)
            return Optional.empty()
        }
        return roles.get(0)
    }

    /**
     * Counts how many active projects have team members in this role.
     *
     * @param roleId the {@link Role}.
     * @return count of active projects with this role.
     */
    private long countActiveProjects(UUID roleId) {
        log.debug('Counting usage for role <{}>.', roleId)
        // TODO count is failing, removed for now
        //int count = roleRepo.countUsage(roleId)
        int count = 0
        log.debug('Project <{}> is active in <{}> projects.', roleId, count)
        return count
    }

    public List<DefaultRoleData> getDefaultRoles(){
        return defaultRoles.getRoleData();
    }
}
