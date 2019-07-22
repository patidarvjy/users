package com.docutools.users

import com.docutools.config.jpa.auditing.AuditedEntity
import com.docutools.contacts.ProjectContact
import com.docutools.subscriptions.Account
import com.docutools.users.values.ChecksumAlgorithm
import com.docutools.users.values.Password
import com.docutools.users.values.PersonName
import com.docutools.users.values.ProfilePicture
import com.docutools.users.values.UserSettings
import com.docutools.users.values.UserType
import com.docutools.users.values.VerificationStatus
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Type

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.PrePersist
import javax.persistence.Table
import javax.persistence.Transient
import java.time.ZonedDateTime

import static javax.persistence.CascadeType.ALL
import static javax.persistence.FetchType.LAZY

/**
 * Represents an user in the docutools environment.
 */
@Entity
@Table(name = 'docutools_users')
@ApiModel(value = "User Resource")
class DocutoolsUser extends AuditedEntity{

    @Id
    @ApiModelProperty(value = "Id of the User")
    @Type(type = "pg-uuid") UUID id = UUID.randomUUID()
    @ManyToOne(optional = false) Organisation organisation
    @ApiModelProperty(value = "Username of the User")
    @Column(nullable = false, unique = true, length = 255) String username
    @Embedded PersonName name = new PersonName()
    @ApiModelProperty(value = "Phone of the User")
    @Column(length = 32) String phone = ''
    @ApiModelProperty(value = "Fax of the User")
    String fax
    @ApiModelProperty(value = "Job Title of the User")
    String jobTitle = ''
    @ApiModelProperty(value = "Department of the User")
    String department
    @ApiModelProperty(value = "Internal Id of the User")
    String internalId
    @ApiModelProperty(value = "Email of the User")
    private String email
    @ApiModelProperty(value = "Street of the User")
    String street
    @ApiModelProperty(value = "Zip of the User")
    String zip
    @ApiModelProperty(value = "City of the User")
    String city
    @ApiModelProperty(value = "Comment on the User")
    String comment = ''
    @Embedded UserSettings settings = new UserSettings()
    @ApiModelProperty(value = "Whether the User is active or not")
    boolean active = true
    @Embedded
    private Password password
    @ElementCollection
    @CollectionTable(
            name = "password_log",
            joinColumns = @JoinColumn(name="user_id")
    )
    @Column(name = "password")
    private List<String> passwordLog = new ArrayList<>();
    @Embedded VerificationStatus  verificationStatus = new VerificationStatus()
    @OneToOne(mappedBy = 'owner', fetch = LAZY, cascade = ALL, orphanRemoval = true)
    ProfilePicture avatar
    @ApiModelProperty(value = "Timestamp of when the User was last invited")
    ZonedDateTime lastInvitationTime
    @ManyToOne
    OrganisationName organisationName

    @OneToOne(mappedBy = "user", cascade = ALL)
    Account account
    @Column(name = 'user_type')
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Type of User")
    UserType type = UserType.Password

    @ApiModelProperty(value = "Whether the User is email subscribed or not")
    boolean emailSubscribed = true

    @ApiModelProperty(value = "Checksum of the User avatar thumbnail")
    String avatarThumbnailChecksum = ""
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Checksum Algorithm used for the checksum")
    ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.NONE

    @Transient
    @ApiModelProperty(value = "Whether the user was newly created")
    boolean isNewCreated = false

    @ApiModelProperty(value = "Whether the user accepted the Privacy Policy")
    boolean privacyPolicyAccepted
    @ApiModelProperty(value = "Whether the user accepted the Terms and Conditions")
    boolean termsAndConditionsAccepted
    @OneToOne
    DocutoolsUser invitedBy

    DocutoolsUser() {
    }

    DocutoolsUser(String username, Organisation organisation) {
        this.username = username
        this.organisation = organisation
    }

    Password getPassword() {
        return password
    }

    void setPassword(Password password) {
        this.password = password
        this.passwordLog.add(password.hash)
    }

    List<String> getPasswordLog() {
        return passwordLog
    }

    boolean isOrganisationOwner() {
        id == organisation.owner?.id
    }

    /**
     * Tests whether the user is neither project creator, admin nor owner.
     *
     * @return {@code true} when no authority granted.
     */
    boolean isUnprivileged() {
        !(settings.projectCreator || settings.admin || organisationOwner)
    }

    /**
     * Tests whether the user is either project creator, admin or owner.
     *
     * @return {@code true} when any authority granted.
     */
    boolean isPrivileged() {
        (settings.projectCreator || settings.admin || organisationOwner)
    }

    /**
     * Gets the user's email address or {@code null} if none configured.
     *
     * @return email address
     */
    String getEmail() {
        if(!email && username?.contains('@')) {
            username
        } else {
            email
        }
    }

    void setEmail(String email) {
        this.email = email
    }

    boolean hasActiveAccount() {
        if(isOrganisationOwner() && account == null) {
            def subscription = organisation.subscription
            account = subscription.getFreeAccount().orElseGet({subscription.newAccount().orElse(null)})
            account.assign(this)
        }
        return account != null && account.isActive()
    }

    /**
     * Checks whether this user is admin.
     *
     * @return {@code true} when admin.
     */
    boolean isAdmin() {
        settings.admin || organisationOwner
    }

    boolean isMemberOf(Organisation org) {
        return organisation.id == org?.id
    }

    String getCompanyName() {
        if(organisationName != null)
            return organisationName.name
        return organisation.name
    }

    void copyAttributes(ProjectContact contact) {
        name = new PersonName(firstName: contact.firstName, lastName: contact.lastName)
        phone = contact.phone
        fax = contact.fax
        jobTitle = contact.jobTitle
        department = contact.department
        internalId = contact.internalId
        street = contact.street
        zip = contact.zip
        city = contact.city
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        DocutoolsUser that = (DocutoolsUser) o

        if (active != that.active) return false
        if (comment != that.comment) return false
        if (id != that.id) return false
        if (jobTitle != that.jobTitle) return false
        if (name != that.name) return false
        if (password != that.password) return false
        if (phone != that.phone) return false
        if (settings != that.settings) return false
        if (username != that.username) return false
        if (verificationStatus != that.verificationStatus) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (username != null ? username.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (phone != null ? phone.hashCode() : 0)
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0)
        result = 31 * result + (comment != null ? comment.hashCode() : 0)
        result = 31 * result + (settings != null ? settings.hashCode() : 0)
        result = 31 * result + (active ? 1 : 0)
        result = 31 * result + (password != null ? password.hashCode() : 0)
        result = 31 * result + (verificationStatus != null ? verificationStatus.hashCode() : 0)
        return result
    }

    @PrePersist
    private void usernameToLowerCase(){
        this.username = this.username?.toLowerCase()
    }
}
