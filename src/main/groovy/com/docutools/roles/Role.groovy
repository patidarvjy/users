package com.docutools.roles

import com.docutools.users.DocutoolsUser
import com.docutools.users.Organisation
import org.hibernate.annotations.Type
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.util.Assert

import javax.persistence.*
import java.time.ZonedDateTime

import static com.docutools.exceptions.ExceptionHelper.*

/**
 * Roles are named sets of {@link Privilege}s belonging to an organisation. Roles can only be created and managed by
 * users with the admin authority in their organisation.
 */
@Entity
@Table(name = 'roles')
@EntityListeners(AuditingEntityListener)
class Role {

    @Id
    @Type(type = 'pg-uuid')
    private UUID id = UUID.randomUUID();
    @Column(nullable = false, length = 64)
    private String name;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = 'role_privileges')
    private Set<Privilege> privileges;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisation organisation;
    @ManyToOne(optional = false)
    private DocutoolsUser createdBy;
    @LastModifiedDate
    private ZonedDateTime lastModified;
    private boolean active = true
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    protected Role() {
    }

    Role(String name, Set<Privilege> privileges, Organisation organisation, DocutoolsUser createdBy) {
        this(name, privileges, organisation, createdBy, RoleType.Custom, true)
    }

    Role(String name, Set<Privilege> privileges, Organisation organisation, DocutoolsUser createdBy, RoleType roleType, boolean active){
        setName(name)
        setPrivileges(privileges)
        Assert.notNull(organisation, 'A role requires an organisation!')
        this.organisation = organisation
        Assert.notNull(createdBy, 'A role requires an createdBy user!')
        this.createdBy = createdBy
        this.roleType = roleType;
        this.active = active;
    }

    boolean hasPrivilege(Privilege...privilege) {
        getPrivileges().any {privilege.contains(it)}
    }

    UUID getId() {
        return id
    }

    String getName() {
        return name
    }

    String setName(String newName) {
        if(!newName || newName.length() > 64)
            throw newInputValidationError("A role name must have 64 characters or less. " +
                    "The specified one has ${newName?.length()} characters.")
        this.name = newName
    }

    Set<Privilege> getPrivileges() {
        return DefaultRoles.instance.getPrivilegesForRoleType(this.roleType)
            .orElse(privileges)
    }

    void setPrivileges(Set<Privilege> privileges) {
        this.privileges = privileges ? privileges : []
    }

    Organisation getOrganisation() {
        return organisation
    }

    DocutoolsUser getCreatedBy() {
        return createdBy
    }

    ZonedDateTime getLastModified() {
        return lastModified
    }

    boolean isActive() {
        active
    }

    void setActive(boolean active) {
        this.active = active
    }

    boolean isDefaultRole(){
        return roleType != RoleType.Custom
    }

    RoleType getRoleType() {
        return roleType
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Role role = (Role) o

        if (id != role.id) return false
        if (lastModified != role.lastModified) return false
        if (name != role.name) return false
        if(active != role.active) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (privileges != null ? privileges.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        return result
    }

    String toString() {
        "Role (id: $id, name: $name, active: $active)"
    }
}
