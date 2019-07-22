package com.docutools.users.resources

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "User Info Resource")
class UserInfoDTO {

    @ApiModelProperty(value = "Id of the User")
    UUID id
    @ApiModelProperty(value = "Name of the User")
    String name

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        UserInfoDTO that = (UserInfoDTO) o

        if (id != that.id) return false
        if (name != that.name) return false

        return true
    }
}
