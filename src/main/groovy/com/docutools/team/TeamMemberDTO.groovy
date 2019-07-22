package com.docutools.team

import com.docutools.users.DocutoolsUser
import com.docutools.roles.RoleDTO
import com.docutools.subscriptions.Account
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.hateoas.ResourceSupport

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Resource representation of {@link TeamMembership}.
 */
@ApiModel(value = "Team Member Resource")
class TeamMemberDTO extends ResourceSupport {

    @ApiModelProperty(value = "Id of the User")
    UUID userId
    @ApiModelProperty(value = "Name of the User")
    String name
    @ApiModelProperty(value = "Id of the Company")
    UUID companyId
    @ApiModelProperty(value = "Name of the Company")
    String companyName
    @ApiModelProperty(value = "Email of the User")
    String email
    @ApiModelProperty(value = "Phone of the User")
    String phone
    @ApiModelProperty(hidden = true)
    String license // TODO rename or remove
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            timezone = "UTC"
    )
    @ApiModelProperty(hidden = true)
    Instant licenseUntil // TODO rename or remove
    @ApiModelProperty(value = "Id of the project")
    UUID projectId
    @ApiModelProperty(value = "Name of the Project")
    String projectName
    @ApiModelProperty(value = "Department of the User")
    String department
    @ApiModelProperty(value = "Street of the User")
    String street
    @ApiModelProperty(value = "Zip of the User")
    String zip
    @ApiModelProperty(value = "City of the User")
    String city
    @ApiModelProperty(value = "Membership state of the User")
    MembershipState state = MembershipState.Active
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "Timestamp of when the user was invited")
    Instant invitedSince
    // Only for newly invited members who will receive an email
    @ApiModelProperty(value = "Text that newly invited members will see when tehy get an email (Unimplemented)")
    String invitationText

    // TODO remove, since replaced by single role
    @Deprecated
    @ApiModelProperty(hidden = true)
    Set<RoleDTO> roles
    // To change the role set in a team member
    // TODO remove, since replaced by single role
    @Deprecated
    @ApiModelProperty(hidden = true)
    Set<UUID> roleIds
    RoleDTO role
    @ApiModelProperty(value = "Id of the role for the User")
    UUID roleId

    @ApiModelProperty(value = "List of the permissions the user has")
    List<TeamMemberPermissions> permissions = []

    // TODO what when NULL
    Account licenseInfo // TODO update
    Account account
    String username
    String countryCode

    TeamMemberDTO(TeamMembership membership) {
        this(membership.user, membership.projectId)
        state = membership.state
        if(state == MembershipState.Active) {
            if(membership.user.verificationStatus.verificationRequired) {
                state = MembershipState.Invited
            }
        }
        invitedSince = membership.invited?.toInstant()
        if(membership.role != null) {
            role = new RoleDTO(membership.role)
            roles = [role]
        }
    }

    TeamMemberDTO(DocutoolsUser user, UUID projectId) {
        userId = user.id
        name = user.name.toString()
        companyId = user.organisation.id
        companyName = user.companyName
        email = user.email
        username = user.username ?: user.email
        phone = user.phone
        license = user.account != null? user.account.type : "Test"
        licenseUntil = user.account?.activeUntil?.atTime(LocalTime.NOON)?.toInstant(ZoneOffset.UTC)
        account = user.account
        licenseInfo = user.account
        this.projectId = projectId
        projectName = ''
        department = user.department
        street = user.street
        zip = user.zip
        city = user.city
        countryCode = user.organisation?.cc
        roles = [] as Set
        state = MembershipState.Inactive
    }

    TeamMemberDTO() {
    }

}
