package com.docutools.roles

import com.docutools.users.resources.UserInfoDTO
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import java.time.Instant

/**
 * Resource representation of {@link Role}.
 */
@ApiModel(value = "Role Resource")
class RoleDTO {

    @ApiModelProperty(value = "Id of the Role")
    UUID id
    @ApiModelProperty(value = "Name of the Role")
    String name
    @ApiModelProperty(value = "The privileges of the Role")
    Set<Privilege> privileges
    UserInfoDTO createdBy
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "When the role was last modified")
    Instant lastModified
    @ApiModelProperty(value = "Whether the role is active or not")
    Boolean active
    @ApiModelProperty(value = "Type of the role")
    RoleType type

    long activeProjects

    RoleDTO(Role role, long activeProjects = 0) {
        id = role.id
        name = role.name
        privileges = new HashSet<>(role.privileges)
        privileges?.remove(Privilege.ManagePlanFolders)
        createdBy = new UserInfoDTO(id: role.createdBy.id, name: role.createdBy.name.toString())
        lastModified = role.lastModified?.toInstant()
        active = role.active
        type = role.roleType

        this.activeProjects = activeProjects
    }

    Set<Privilege> getPrivileges() {
        privileges?.remove(Privilege.ManagePlanFolders)
        return privileges
    }

    RoleDTO() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        RoleDTO roleDTO = (RoleDTO) o

        if (active != roleDTO.active) return false
        if (activeProjects != roleDTO.activeProjects) return false
        if (createdBy != roleDTO.createdBy) return false
        if (id != roleDTO.id) return false
        if (name != roleDTO.name) return false
        if (privileges != roleDTO.privileges) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (privileges != null ? privileges.hashCode() : 0)
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0)
        result = 31 * result + (active ? 1 : 0)
        result = 31 * result + (int) (activeProjects ^ (activeProjects >>> 32))
        return result
    }

    @Override
    String toString() {
        name
    }
}
