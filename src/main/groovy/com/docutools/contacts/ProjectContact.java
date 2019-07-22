package com.docutools.contacts;

import com.docutools.exceptions.ExceptionHelper;
import com.docutools.roles.PermissionManager;
import com.docutools.roles.Privilege;
import com.docutools.roles.PrivilegeCheckDTO;
import com.docutools.users.DocutoolsUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.*;

import static com.docutools.exceptions.ErrorCodes.INVALID_RESOURCE;

/**
 * A Contact represents a Company or Person who can be assigned to tasks in a Project but is not an actual docu tools
 * user.
 *
 * @author amp
 * @since 1.0.0
 */
@Table(name = "project_contacts")
@Entity
@EntityListeners(AuditingEntityListener.class)
@ApiModel(value = "Contact Resource")
public class ProjectContact {

    @Id
    @Type(type = "pg-uuid")
    @ApiModelProperty(value = "Id of the Contact")
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    @ApiModelProperty(value = "Id of the project")
    private UUID projectId;

    @Column(nullable = false)
    @ApiModelProperty(value = "Email of the Contact")
    private String email = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Company Name of the Contact")
    private String companyName = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "First Name of the Contact")
    private String firstName = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Last Name of the Contact")
    private String lastName = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Phone of the Contact")
    private String phone = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Fax of the Contact")
    private String fax = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Job Title of the Contact")
    private String jobTitle = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Department of the Contact")
    private String department = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Internal Id of the Contact")
    private String internalId = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Street of the Contact")
    private String street = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Zip Code of the Contact")
    private String zip = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "City of the Contact")
    private String city = "";
    @CreatedDate
    @ApiModelProperty(value = "Timestamp of the Contact Creation")
    private ZonedDateTime created;
    @LastModifiedDate
    @ApiModelProperty(value = "Timestamp of the last edit of the Contact")
    private ZonedDateTime lastModified;
    @Column(nullable = false)
    @ApiModelProperty(value = "Country Code (2 Char Limit)")
    private String countryCode = "";
    @Column(nullable = false)
    @ApiModelProperty(value = "Whether the user was a contact previously")
    private boolean replaced = false;
    @OneToOne
    @JsonIgnore
    private DocutoolsUser replacedBy;

    @JsonInclude
    @Transient
    @ApiModelProperty(value = "Permissions the current user has on this Contact")
    private List<Permission> permissions;

    public ProjectContact() {
    }

    public ProjectContact(UUID projectId) {
        setProjectId(projectId);
    }

    public ProjectContact(UUID projectId, ProjectContact copyFrom) {
        this.projectId = projectId;
        this.email = copyFrom.getEmail();
        this.companyName = copyFrom.getCompanyName();
        this.firstName = copyFrom.getFirstName();
        this.lastName = copyFrom.getLastName();
        this.phone = copyFrom.getPhone();
        this.fax = copyFrom.getFax();
        this.jobTitle = copyFrom.getJobTitle();
        this.department = copyFrom.getDepartment();
        this.internalId = copyFrom.getInternalId();
        this.street = copyFrom.getStreet();
        this.zip = copyFrom.getZip();
        this.city = copyFrom.getCity();
        this.countryCode = copyFrom.getCountryCode();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if(email != null)
            this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if(companyName != null)
            this.companyName = companyName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if(firstName != null)
            this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if(lastName != null)
            this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if(phone != null)
            this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        if(fax != null)
            this.fax = fax;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        if(jobTitle != null)
            this.jobTitle = jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if(department != null)
            this.department = department;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        if(internalId != null)
            this.internalId = internalId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        if(street != null)
            this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        if(zip != null)
            this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if(city != null)
            this.city = city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        if(countryCode != null) {
            if (countryCode.length() > 2) {
                throw ExceptionHelper.newBadRequestError(INVALID_RESOURCE,
                        String.format("CountryCode does not allow more than 2 characters! (value: %s)", countryCode));
            }
            this.countryCode = countryCode;
        }
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Sets this user as replacement for the contact.
     *
     * @param user the user
     */
    @JsonIgnore
    public void replaceBy(DocutoolsUser user) {
        Assert.notNull(user, "user is required - must not be NULL!");
        this.replaced = true;
        this.replacedBy = user;
    }

    public boolean isReplaced() {
        return replaced;
    }

    @JsonIgnore
    public DocutoolsUser getReplacedBy() {
        return replacedBy;
    }

    public UUID getReplacedById() {
        return replacedBy == null? null : replacedBy.getId();
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public ProjectContact withPermissions(PermissionManager manager) {
        PrivilegeCheckDTO check = manager.checkPrivilege(projectId, Arrays.asList(Privilege.ViewTeam, Privilege.ManageTeam), true);
        if(!check.isCheck())
            permissions = Collections.emptyList();
        permissions = new ArrayList<>(2);
        permissions.add(Permission.View);
        if(check.getPrivileges().contains(Privilege.ManageTeam)) {
            permissions.add(Permission.Edit);
            permissions.add(Permission.Delete);
        }
        return this;
    }

    @JsonIgnore
    public boolean canEdit() {
        return permissions != null && permissions.contains(Permission.Edit);
    }

    @JsonIgnore
    public boolean canView() {
        return permissions != null && permissions.contains(Permission.View);
    }

    /**
     * Suggests the best possible display name for the Contact:
     * <ul>
     *     <li>First looking if first and/or last name is set and returning first name + ' ' + last name.</li>
     *     <li><b>Iff not</b> then return company name when not empty.</li>
     *     <li>Else return email.</li>
     * </ul>
     *
     * @return best possible display name
     */
    @JsonIgnore
    public String suggestName() {
        String name = "";
        if(!StringUtils.isEmpty(firstName))
            name = firstName;
        if(!StringUtils.isEmpty(lastName)) {
            if(!name.isEmpty()) {
                name += " ";
            }
            name += lastName;
        }
        if(!name.isEmpty()) {
            return name;
        }
        if(!StringUtils.isEmpty(companyName)) {
            return companyName;
        }
        return email;
    }

    /**
     * Similar to {@link this#suggestName()} but step 1 and 2 are switched.
     *
     * @return best possible company display name
     */
    @JsonIgnore
    public String suggestCompanyName() {
        if(!StringUtils.isEmpty(companyName)) {
            return companyName;
        }
        String name = "";
        if(!StringUtils.isEmpty(firstName))
            name = firstName;
        if(!StringUtils.isEmpty(lastName)) {
            if(!name.isEmpty()) {
                name += " ";
            }
            name += lastName;
        }
        if(!name.isEmpty()) {
            return name;
        }
        return email;
    }

    public String getDisplayName() {
        return suggestName();
    }

    public String getDisplayCompanyName() {
        return suggestCompanyName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectContact that = (ProjectContact) o;

        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) return false;
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) return false;
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) return false;
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) return false;
        if (fax != null ? !fax.equals(that.fax) : that.fax != null) return false;
        if (jobTitle != null ? !jobTitle.equals(that.jobTitle) : that.jobTitle != null) return false;
        if (department != null ? !department.equals(that.department) : that.department != null) return false;
        if (internalId != null ? !internalId.equals(that.internalId) : that.internalId != null) return false;
        if (street != null ? !street.equals(that.street) : that.street != null) return false;
        if (zip != null ? !zip.equals(that.zip) : that.zip != null) return false;
        return city != null ? city.equals(that.city) : that.city == null;
    }

    @Override
    public int hashCode() {
        int result = projectId != null ? projectId.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0);
        result = 31 * result + (department != null ? department.hashCode() : 0);
        result = 31 * result + (internalId != null ? internalId.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProjectContact{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", email='" + email + '\'' +
                ", companyName='" + companyName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", created=" + created +
                ", lastModified=" + lastModified +
                '}';
    }

    public static String getComaseparatedFieldNames() {
        return "firstName,lastName,companyName,email,phone,fax,jobTitle,department,street,zip,city,countryCode\n";
    }

    public String toCommaSeparated() {
        return firstName + "," +
                lastName + "," +
                companyName + "," +
                email + "," +
                phone + "," +
                fax + "," +
                jobTitle + "," +
                department + "," +
                street + "," +
                zip + "," +
                city + "," +
                countryCode;
    }

    public enum Permission {
        View,
        Edit,
        Delete,
    }
}
