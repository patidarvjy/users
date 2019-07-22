package com.docutools.users.resources

import com.docutools.subscriptions.SubscriptionType
import com.docutools.users.DocutoolsUser
import com.docutools.users.MockedLicense
import com.docutools.users.values.UserType
import com.docutools.utils.Validator
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Resource representation of {@link DocutoolsUser}.
 */
@ApiModel(value = "User Resource")
class UserDTO extends AuditedDTO{

    @ApiModelProperty(value = "Id of the User")
    UUID userId
    @ApiModelProperty(value = "Id of the Organisation")
    UUID organisationId
    @ApiModelProperty(value = "Id of the Organisation Name")
    UUID organisationNameId
    @ApiModelProperty(value = "Organisation Name")
    String organisationName
    @ApiModelProperty(value = "Username of the User")
    String username
    @ApiModelProperty(value = "Email of the User")
    String email
    @ApiModelProperty(value = "First Name of the User")
    String firstName
    @ApiModelProperty(value = "Last Name of the User")
    String lastName
    @ApiModelProperty(value = "Phone of the User")
    String phone
    @ApiModelProperty(value = "Fax of the User")
    String fax
    @ApiModelProperty(value = "Job Title of the User")
    String jobTitle
    @ApiModelProperty(value = "Department of the User")
    String department
    @ApiModelProperty(value = "Internal Id of the User")
    String internalId
    @ApiModelProperty(value = "Street of the User")
    String street
    @ApiModelProperty(value = "Zip of the User")
    String zip
    @ApiModelProperty(value = "City of the User")
    String city
    @ApiModelProperty(value = "Comment on the User")
    String comment
    SettingsDTO settings
    @ApiModelProperty(value = "Whether the User is the owner or not")
    Boolean owner
    @ApiModelProperty(value = "Whether the User is active or not")
    Boolean active
    @ApiModelProperty(value = "Type of the User")
    UserType type
    @ApiModelProperty(value = "Whether the User accepted the Privacy Policy")
    Boolean privacyPolicyAccepted
    @ApiModelProperty(value = "Whether the User accepted the Terms and Conditions")
    Boolean termsAndConditionsAccepted

    // Email user attributes
    @ApiModelProperty(value = "Whether the user is verified or not")
    boolean verified

    @ApiModelProperty(value = "List of Permissions the User hsa in the Organisation")
    List<OrganisationPermissions> permissions
    @ApiModelProperty(value = "Invistation Status of the User")
    InvitationStatus invitationStatus

    MockedLicense license

    @ApiModelProperty(value = "Message that is shown if the User has no License")
    String noLicenseMessage
    @ApiModelProperty(value = "Country code")
    String countryCode

    @ApiModelProperty(value = "If true, the user must have SMS/MFA enabled.")
    boolean forceMfa = false


    UserDTO(DocutoolsUser entity) {
        super(entity)
        userId = entity.id
        organisationId = entity.organisation?.id
        countryCode = entity.organisation?.cc
        organisationName = entity.companyName
        if(!entity.email) {
            username = entity.username
        } else {
            username = entity.email
        }
        firstName = entity.name?.firstName
        lastName = entity.name?.lastName
        if(!firstName && !lastName) {
            firstName = username
        }
        email = entity.email
        phone = entity.phone
        fax = entity.fax
        jobTitle = entity.jobTitle
        department = entity.department
        internalId = entity.internalId
        street = entity.street
        zip = entity.zip
        city = entity.city
        comment = entity.comment
        settings = new SettingsDTO(entity.settings)
        owner = entity.isOrganisationOwner()
        active = entity.active
        verified = !entity.verificationStatus.verificationRequired
        permissions = new ArrayList<>()
        privacyPolicyAccepted = entity.privacyPolicyAccepted
        termsAndConditionsAccepted = entity.termsAndConditionsAccepted
        organisationNameId = entity.organisationName? entity.organisationName.id : entity.organisation.id
        if (entity.hasActiveAccount() || true) { // todo
            if (entity.settings.admin) {
                permissions.addAll(Arrays.asList(OrganisationPermissions.values()))
                permissions.remove(OrganisationPermissions.GrantAdmin)
                permissions.remove(OrganisationPermissions.ManageLicenses)
            }
            if (entity.settings.projectCreator)
                permissions.addAll(Arrays.asList(OrganisationPermissions.CreateProjects, OrganisationPermissions.CreateProjectFolders, OrganisationPermissions.ViewUsers, OrganisationPermissions.ViewReportTemplates, OrganisationPermissions.EditReportTemplates))
            if (entity.isOrganisationOwner()) {
                permissions.add(OrganisationPermissions.GrantAdmin)
                //permissions.add(OrganisationPermissions.ManageLicenses)
            }
        }
        if(Validator.isSustainUser(username)) {
            permissions.add(OrganisationPermissions.ViewMaestro)
        } else {
            permissions.remove(OrganisationPermissions.ViewMaestro)
        }

        def until = entity.account ? entity.account.activeUntil :entity.organisation.getSubscription().until
        def since = entity.account ? entity.account.subscription.since.atStartOfDay().toInstant(ZoneOffset.UTC) : entity.created?.toInstant()
        license = new MockedLicense(entity.account?.type, until
                ?.atTime(LocalTime.of(23, 59))
                ?.atZone(ZoneId.of("UTC"))
                ?.toInstant(),
                since)

        invitationStatus = entity.verificationStatus?.verificationRequired ?
                InvitationStatus.Pending :
                (entity.active ? InvitationStatus.Active : InvitationStatus.Inactive)
        type = entity.type

        if(!entity.hasActiveAccount()) {
            Map<String, String> noLicenseMessages = entity.organisation.getNoLicenseMessages()
            if(settings.language && noLicenseMessages.containsKey(settings.language))
                noLicenseMessage = noLicenseMessages.get(settings.language)
            else if(noLicenseMessages.containsKey("en"))
                noLicenseMessage = noLicenseMessages.get("en")
        }
        forceMfa = entity.organisation.forceMfa

    }

    UserDTO() {
    }

    /*
     * Checks the DTO and returns true if the only field to be updated is the organisationNameId
     * Reference Method: {@link com.docutools.users.UserManager#updateUser(DocutoolsUser, UserDTO) UpdateUser}
     */
    boolean updatesOrganisationNameOnly(){
        if(firstName != null) return false
        if(lastName != null) return false
        if(phone != null) return false
        if(fax != null) return false
        if(jobTitle != null) return false
        if(department != null) return false
        if(internalId != null) return false
        if(street != null) return false
        if(zip != null) return false
        if(city != null) return false
        if(settings != null) return false
        return organisationNameId != null
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        UserDTO userDTO = (UserDTO) o

        if (verified != userDTO.verified) return false
        if (active != userDTO.active) return false
        if (city != userDTO.city) return false
        if (comment != userDTO.comment) return false
        if (countryCode != userDTO.countryCode) return false
        if (department != userDTO.department) return false
        if (email != userDTO.email) return false
        if (fax != userDTO.fax) return false
        if (firstName != userDTO.firstName) return false
        if (internalId != userDTO.internalId) return false
        if (invitationStatus != userDTO.invitationStatus) return false
        if (jobTitle != userDTO.jobTitle) return false
        if (lastName != userDTO.lastName) return false
        if (organisationId != userDTO.organisationId) return false
        if (organisationName != userDTO.organisationName) return false
        if (owner != userDTO.owner) return false
        if (permissions != userDTO.permissions) return false
        if (phone != userDTO.phone) return false
        if (settings != userDTO.settings) return false
        if (street != userDTO.street) return false
        if (userId != userDTO.userId) return false
        if (username != userDTO.username) return false
        if (zip != userDTO.zip) return false

        return true
    }

    int hashCode() {
        int result
        result = (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (organisationId != null ? organisationId.hashCode() : 0)
        result = 31 * result + (organisationName != null ? organisationName.hashCode() : 0)
        result = 31 * result + (username != null ? username.hashCode() : 0)
        result = 31 * result + (email != null ? email.hashCode() : 0)
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0)
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0)
        result = 31 * result + (phone != null ? phone.hashCode() : 0)
        result = 31 * result + (fax != null ? fax.hashCode() : 0)
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0)
        result = 31 * result + (department != null ? department.hashCode() : 0)
        result = 31 * result + (internalId != null ? internalId.hashCode() : 0)
        result = 31 * result + (street != null ? street.hashCode() : 0)
        result = 31 * result + (zip != null ? zip.hashCode() : 0)
        result = 31 * result + (city != null ? city.hashCode() : 0)
        result = 31 * result + (comment != null ? comment.hashCode() : 0)
        result = 31 * result + (settings != null ? settings.hashCode() : 0)
        result = 31 * result + (owner != null ? owner.hashCode() : 0)
        result = 31 * result + (active != null ? active.hashCode() : 0)
        result = 31 * result + (verified ? 1 : 0)
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0)
        result = 31 * result + (invitationStatus != null ? invitationStatus.hashCode() : 0)
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0)
        return result
    }
}
