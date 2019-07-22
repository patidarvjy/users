package com.docutools.team

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import java.util.function.Function

@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "Team Member Bulk Resource")
class TeamMemberBulkDTO {

    @ApiModelProperty(value = "List of the Ids of the Users")
    List<UUID> userIds
    @ApiModelProperty(value = "List of the emails of the users")
    List<String> emails
    @ApiModelProperty(value = "Whether the users should be active or not")
    boolean active
    @ApiModelProperty(value = "Id of the role the users should have")
    UUID roleId
    @ApiModelProperty(value = "The Welcome Message the users should see")
    String welcomeMessage

    TeamMemberBulkDTO() {
    }

    int count() {
        0 + (userIds? userIds.size() : 0) + (emails? emails.size() : 0)
    }

    @JsonIgnore
    <T> List<T> collectUserIds(Function<UUID, T> function) {
        (userIds? userIds : []).collect {function.apply(it)}
    }

    @JsonIgnore
    <T> List<T> collectEmails(Function<String, T> function) {
        (emails? emails : []).collect {function.apply(it)}
    }

}
