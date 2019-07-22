package com.docutools.team

import com.docutools.users.DocutoolsUser
import com.docutools.roles.Privilege
import com.docutools.roles.Role
import org.hibernate.annotations.Type
import org.springframework.util.Assert

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import java.time.ZonedDateTime

import static com.docutools.exceptions.ExceptionHelper.*;

/**
 * Represents the {@link Role}s a {@link DocutoolsUser} takes in a project.
 */
@Entity
@Table(name = 'team_memberships',
    uniqueConstraints = [@UniqueConstraint(columnNames = ['project_id', 'user_id'])])
class TeamMembership {

    @Id
    @Type(type = 'pg-uuid')
    private UUID id = UUID.randomUUID()
    @ManyToOne(optional = false)
    private DocutoolsUser user
    @Column(nullable = false, name = 'project_id')
    @Type(type = "pg-uuid")
    private UUID projectId
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = 'role_assignments',
        joinColumns = @JoinColumn(name = 'member_id'),
        inverseJoinColumns = @JoinColumn(name = 'role_id'))
    private Set<Role> roles = new HashSet<>()
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    private Role role;
    @Column(nullable = false)
    private ZonedDateTime invited = ZonedDateTime.now()
    @Enumerated(EnumType.STRING)
    private MembershipState state
    @Column(name = 'last_modified')
    private ZonedDateTime lastModified
    @Column(name = 'last_modified_by')
    private UUID lastModifiedBy

    protected TeamMembership() {
    }

    /**
     * Create a new {@link TeamMembership} for a {@link DocutoolsUser} and a specific project with an initial Membership
     * State.
     *
     * @param user the member.
     * @param projectId the project's id.
     * @param state the initial state.
     */
    TeamMembership(DocutoolsUser user, UUID projectId, MembershipState state) {
        Assert.notNull(user, 'A TeamMembership entity requires a DocutoolsUses.')
        this.user = user
        Assert.notNull(projectId, "Project's ID must not be NULL!")
        this.projectId = projectId
        setState(state)
    }

    boolean hasPrivilege(Privilege...privileges) {
        getRole().hasPrivilege(privileges)
    }

    UUID getId() {
        return id
    }

    DocutoolsUser getUser() {
        return user
    }

    UUID getProjectId() {
        return projectId
    }

    Set<Role> getRoles() {
        return roles
    }

    void setRoles(Set<Role> roles) {
        if(roles) {
            this.roles = roles
        } else
            this.roles = new HashSet<>()
    }

    Role getRole() {
        if(role == null && roles.size() > 0) {
            role = roles.max {it.roleType.order}
        }
        return role
    }

    void setRole(Role role) {
        this.role = role
    }

    ZonedDateTime getInvited() {
        return invited
    }

    MembershipState getState() {
        return state
    }

    void setState(MembershipState state) {
        if(state == null)
            throw newInputValidationError('Membership state is required!')
        this.state = state
    }

    ZonedDateTime getLastModified() {
        return lastModified
    }

    void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified
    }

    UUID getLastModifiedBy() {
        return lastModifiedBy
    }

    void setLastModifiedBy(UUID lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TeamMembership that = (TeamMembership) o

        if (projectId != that.projectId) return false
        if (id != that.id) return false
        if (invited != that.invited) return false
        if (roles != that.roles) return false
        if (state != that.state) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0)
        result = 31 * result + (roles != null ? roles.hashCode() : 0)
        result = 31 * result + (invited != null ? invited.hashCode() : 0)
        result = 31 * result + (state != null ? state.hashCode() : 0)
        return result
    }

    String toString() {
        "Membership (id: $id, user: ${user.name.toString()}($user.id), projectId: $projectId)"
    }
}
