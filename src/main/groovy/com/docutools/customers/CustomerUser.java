package com.docutools.customers;

import com.docutools.customers.values.Name;
import com.docutools.users.DocutoolsUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiModel(value = "Customer User Resource")
public class CustomerUser extends ResourceSupport {

    private static final String USER_ADMIN_ROLE = "User Admin", PROJECT_ADMIN_ROLE = "Project Admin";

    @ApiModelProperty(value = "Id of the User")
    private UUID userId;
    private Name name;
    @ApiModelProperty(value = "Email of the User")
    private String email;
    @ApiModelProperty(value = "Language of the User")
    private String language;
    @ApiModelProperty(value = "List of the users roles")
    private List<String> roles;

    @JsonCreator
    public CustomerUser(@JsonProperty("userId") UUID userId,
                        @JsonProperty("email") String email) {
        this.userId = userId;
        this.email = email;
    }

    public CustomerUser(DocutoolsUser user) {
        this.userId = user.getId();
        this.name = new Name(user.getName());
        this.email = user.getUsername();
        this.language = user.getSettings().getLanguage();
        this.roles = new ArrayList<>(2);
        if(user.getSettings().isAdmin()) {
            this.roles.add(USER_ADMIN_ROLE);
            this.roles.add(PROJECT_ADMIN_ROLE);
        } else if(user.getSettings().isProjectCreator()) {
            this.roles.add(PROJECT_ADMIN_ROLE);
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public Name getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CustomerUser that = (CustomerUser) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name  != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (language != null ? !language.equals(that.language) : that.language != null) return false;
        return roles != null ? roles.equals(that.roles) : that.roles == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomerUser{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", language='" + language + '\'' +
                ", roles=" + roles +
                '}';
    }
}
