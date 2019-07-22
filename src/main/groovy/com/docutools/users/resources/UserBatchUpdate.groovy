package com.docutools.users.resources

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "User Batch Update Resource")
class UserBatchUpdate {

    @JsonProperty(required = true)
    @ApiModelProperty(value = "List of User ids to update")
    List<UUID> ids
    @JsonProperty(required = true)
    UserDTO update

}
