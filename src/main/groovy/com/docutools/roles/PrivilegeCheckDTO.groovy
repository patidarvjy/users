package com.docutools.roles

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Privilege Check Resource")
class PrivilegeCheckDTO {

    @ApiModelProperty(value = "List of the Privileges")
    List<Privilege> privileges
    @ApiModelProperty(value = "Id of the Project")
    UUID projectId
    @ApiModelProperty(value = "Id of the Current User")
    UUID currentUserId
    @ApiModelProperty(value = "Whether if any/all privilege should result in checked")
    boolean any
    @ApiModelProperty(value = "Whether the user has the given privileges")
    boolean check

    PrivilegeCheckDTO(List<Privilege> privileges, UUID projectId, boolean any, boolean check) {
        this.privileges = privileges
        this.projectId = projectId
        this.any = any
        this.check = check
    }

    PrivilegeCheckDTO() {
    }

    List<Privilege> getPrivileges() {
        privileges?.remove(Privilege.ManagePlanFolders)
        return privileges
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PrivilegeCheckDTO that = (PrivilegeCheckDTO) o

        if (any != that.any) return false
        if (check != that.check) return false
        if (projectId != that.projectId) return false
        if (privileges != that.privileges) return false

        return true
    }

    int hashCode() {
        int result
        result = (privileges != null ? privileges.hashCode() : 0)
        result = 31 * result + (int) (projectId ^ (projectId >>> 32))
        result = 31 * result + (any ? 1 : 0)
        result = 31 * result + (check ? 1 : 0)
        return result
    }
}
