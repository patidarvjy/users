package com.docutools.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * One of many names for an {@link Organisation} which can be assigned to one or many of its {@link DocutoolsUser}s.
 *
 * @author amp
 * @version 1.0.0
 */
@Entity
@Table(name = "organisation_names", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "organisation_id"})})
@ApiModel(value = "Organisation Name Resource")
public class OrganisationName {

    @Id
    @Type(type = "pg-uuid")
    @ApiModelProperty(value = "Id of the Organisation Name")
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    @ApiModelProperty(value = "Name of the Organisation Name")
    private String name;
    @ManyToOne(optional = false)
    private Organisation organisation;
    @OneToMany(mappedBy = "organisationName")
    @ApiModelProperty(value = "List of Users using the Organisation Name")
    private List<DocutoolsUser> users = new ArrayList<>();
    @JsonInclude
    @Transient
    @ApiModelProperty(value = "The Permissions the current User has for this Organisation Name")
    private List<Permission> permissions;

    private OrganisationName() {
    }

    @JsonCreator
    public OrganisationName(@JsonProperty(value = "name", required = true) String name) {
        this.name = name;
    }

    public OrganisationName(String name, Organisation organisation) {
        setName(name);
        this.organisation = organisation;
    }

    public OrganisationName(String name, Organisation organisation, UUID id) {
        this(name, organisation);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Assert.notNull(name, "name is required - must not be NULL!");
        Assert.isTrue(!StringUtils.isEmpty(name), "name is required - must not be empty!");
        this.name = name;
    }

    @JsonIgnore
    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    @JsonIgnore
    public List<DocutoolsUser> getUsers() {
        return users;
    }

    public List<Permission> getPermissions() {
        if(permissions == null) {
            if (users.isEmpty()) {
                permissions = Collections.singletonList(Permission.Delete);
            } else {
                permissions = Collections.emptyList();
            }
        }
        return permissions;
    }

    public void setPermissions(List<Permission> list) {
        if (list != null) {
            permissions = list;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganisationName that = (OrganisationName) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    public enum Permission {
        Delete
    }
}
