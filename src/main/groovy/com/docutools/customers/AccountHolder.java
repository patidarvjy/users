package com.docutools.customers;

import com.docutools.users.DocutoolsUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.UUID;

@ApiModel(value = "Account Holder Resource")
public class AccountHolder {

    @ApiModelProperty(value = "Id of the User holding the account")
    private UUID userId;
    @ApiModelProperty(value = "Name of the User")
    private String name;
    @ApiModelProperty(value = "Email of the User")
    private String email;
    @ApiModelProperty(value = "Whether the user is verified or not")
    private boolean verified;
    @JsonIgnore
    private String token;
    @ApiModelProperty(value = "Activation Link of the user")
    private String activationLink;
    private boolean assigned;

    public AccountHolder(DocutoolsUser user) {
        this.userId = user.getId();
        this.name = user.getName().toString();
        this.email = user.getUsername();
        this.verified = user.getVerificationStatus() != null && user.getVerificationStatus().getVerified();
        if (!verified && user.getVerificationStatus() != null) {
            this.token = user.getVerificationStatus().getToken();
        }
        assigned = user.getAccount() != null;
    }

    @JsonCreator
    public AccountHolder(@JsonProperty(value = "userId", required = true) UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountHolder that = (AccountHolder) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return email != null ? email.equals(that.email) : that.email == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccountHolder{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public void generateActivationLink(String url) {
        activationLink = verified ? "" : String.format("%s/%s", url, token);
    }

    public String getActivationLink() {
        return activationLink;
    }

    public boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}
