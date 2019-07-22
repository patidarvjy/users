package com.docutools.assignees;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.team.MembershipState;
import com.docutools.contacts.ProjectContact;
import com.docutools.team.TeamMembership;
import com.docutools.users.OrganisationName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;
import java.util.UUID;

@ApiModel(value = "Report Resource")
public class Assignee {

    @ApiModelProperty(value = "Id of the assignee")
    private UUID id;
    @ApiModelProperty(value = "Name of the assignee")
    private String name;
    @ApiModelProperty(value = "Id of the assignee company")
    private UUID companyId;
    @ApiModelProperty(value = "Name of the assingee company")
    private String companyName;
    @ApiModelProperty(value = "Type of assignee")
    private AssigneeType type;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @ApiModelProperty(value = "Timestamp of when it was last modified")
    private Instant lastModified;
    @ApiModelProperty(value = "State of this assignee")
    private MembershipState state;

    @JsonCreator
    public Assignee(@JsonProperty(value = "id", required = true) UUID id,
                    @JsonProperty("name") String name,
                    @JsonProperty("companyId") UUID companyId,
                    @JsonProperty("companyName") String companyName,
                    @JsonProperty("type") AssigneeType type,
                    @JsonProperty("lastModified") Instant lastModified,
                    @JsonProperty("state") MembershipState state) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.companyName = companyName;
        this.type = type;
        this.lastModified = lastModified;
        this.state = state;
    }

    public Assignee(TeamMembership membership) {
        this(membership.getUser(), membership.getState());
    }

    public Assignee(ProjectContact contact) {
        this.id = contact.getId();
        this.name = contact.suggestName();
        this.companyId = contact.getId();
        this.companyName = contact.suggestCompanyName();
        this.type = AssigneeType.Contact;
        if(contact.getLastModified() != null) {
            this.lastModified = contact.getLastModified().toInstant();
        }
        this.state = MembershipState.Active;
    }

    public Assignee(DocutoolsUser user) {
        this(user, MembershipState.Active);
    }

    public Assignee(DocutoolsUser user, MembershipState state) {
        this.id = user.getId();
        this.name = user.getName().toString();
        this.type = AssigneeType.User;
        if(user.getLastModified() != null) {
            this.lastModified = user.getLastModified().toInstant();
        }
        this.state = state;
        if(user.getOrganisationName() != null) {
            OrganisationName name = user.getOrganisationName();
            this.companyId = name.getId();
            this.companyName = name.getName();
        } else {
            Organisation organisation = user.getOrganisation();
            this.companyId = organisation.getId();
            this.companyName = organisation.getName();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public AssigneeType getType() {
        return type;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public MembershipState getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assignee assignee = (Assignee) o;

        if (id != null ? !id.equals(assignee.id) : assignee.id != null) return false;
        if (name != null ? !name.equals(assignee.name) : assignee.name != null) return false;
        if (companyId != null ? !companyId.equals(assignee.companyId) : assignee.companyId != null) return false;
        if (companyName != null ? !companyName.equals(assignee.companyName) : assignee.companyName != null)
            return false;
        if (type != assignee.type) return false;
        return state == assignee.state;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (companyId != null ? companyId.hashCode() : 0);
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Assignee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", companyId=" + companyId +
                ", companyName='" + companyName + '\'' +
                ", type=" + type +
                ", state=" + state +
                '}';
    }

}
