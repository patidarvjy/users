package com.docutools.assignees;

import com.docutools.contacts.ProjectContact;
import com.docutools.team.MembershipState;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class AssigneeCompany {

    private UUID id;
    private String name;
    private Assignee defaultAssignee;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant lastModified;

    @JsonCreator
    public AssigneeCompany(@JsonProperty("id") UUID id,
                           @JsonProperty("name") String name,
                           @JsonProperty("defaultAssignee") Assignee defaultAssignee,
                           @JsonProperty("lastModified") Instant lastModified) {
        this.id = id;
        this.name = name;
        this.defaultAssignee = defaultAssignee;
        this.lastModified = lastModified;
    }

    public AssigneeCompany(DocutoolsUser user) {
        this(user, MembershipState.Active);
    }

    public AssigneeCompany(DocutoolsUser user, MembershipState membershipState) {
        this.defaultAssignee = new Assignee(user, membershipState);
        Organisation organisation = user.getOrganisation();
        if(organisation.getLastModified() != null)
            this.lastModified = organisation.getLastModified().toInstant();
        if(user.getOrganisationName() != null) {
            OrganisationName orgaName = user.getOrganisationName();
            this.id = orgaName.getId();
            this.name = orgaName.getName();
        } else {
            this.id = organisation.getId();
            this.name = organisation.getName();
        }
    }

    public AssigneeCompany(Organisation organisation, DocutoolsUser defaultAssignee) {
        this.id = organisation.getId();
        this.name = organisation.getName();
        this.defaultAssignee = new Assignee(defaultAssignee);
        if(organisation.getLastModified() != null) {
            this.lastModified = organisation.getLastModified().toInstant();
        }
    }

    public AssigneeCompany(ProjectContact contact) {
        this.id = contact.getId();
        this.name = contact.suggestCompanyName();
        this.defaultAssignee = new Assignee(contact);
        if(contact.getLastModified() != null) {
            this.lastModified = contact.getLastModified().toInstant();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Assignee getDefaultAssignee() {
        return defaultAssignee;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssigneeCompany that = (AssigneeCompany) o;

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
        return "AssigneeCompany{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", defaultAssignee=" + defaultAssignee +
                ", lastModified=" + lastModified +
                '}';
    }
}
