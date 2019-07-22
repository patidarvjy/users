package com.docutools.customers;

import com.docutools.subscriptions.Subscription;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.resources.UserDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@ApiModel(value = "Contact Resource")
public class Customer extends ResourceSupport {

    @ApiModelProperty(value = "Id of the organisation")
    private UUID organisationId;
    @ApiModelProperty(value = "Name of the customer")
    private String name;
    @ApiModelProperty(value = "CC of the customer")
    private String cc;
    @ApiModelProperty(value = "Billing Mail of the customer")
    private String billingMail;
    private Subscription subscription;
    private CustomerUser owner;
    @JsonFormat(pattern = "MM-dd-yyyy")
    @ApiModelProperty(value = "Timestamp of the Creation of the Customer")
    private LocalDate created;
    @ApiModelProperty(value = "Reseller")
    private String reseller;
    @ApiModelProperty(value = "True, if this customer has client credentials set.")
    private boolean hasClientCredentials;
    @ApiModelProperty(value = "True, if this customer is invited by other.")
    private boolean invited;
    @ApiModelProperty(value = "User, if this customer is invited by other.")
    private UserDTO invitedBy;

    @JsonCreator
    public Customer(@JsonProperty("name") String name,
                    @JsonProperty("cc") String cc,
                    @JsonProperty("billingMail") String billingMail,
                    @JsonProperty("subscription") Subscription subscription,
                    @JsonProperty("owner") CustomerUser owner) {
        this.name = name;
        this.cc = cc;
        this.billingMail = billingMail;
        this.subscription = subscription;
        this.owner = owner;
    }

    public Customer(UUID organisationId, String name, String cc, String billingMail,
                    Subscription subscription,
                    DocutoolsUser owner, ZonedDateTime created, String reseller, boolean hasClientCredentials) {
        this.organisationId = organisationId;
        this.name = name;
        this.cc = cc;
        this.billingMail = billingMail;
        this.subscription = subscription;
        this.owner = new CustomerUser(owner);
        if (created != null) {
            this.created = created.toLocalDate();
        }
        this.reseller = reseller;
        this.hasClientCredentials = hasClientCredentials;
    }

    public Customer(Organisation organisation) {
        this.organisationId = organisation.getId();
        this.name = organisation.getName();
        this.cc = organisation.getCc() != null ? organisation.getCc().toLowerCase() : "at";
        this.billingMail = organisation.getBillingMail();
        this.subscription = organisation.getSubscription();
        DocutoolsUser owner = organisation.getOwner();
        if (owner != null) {
            this.owner = new CustomerUser(owner);
            if (StringUtils.isEmpty(this.billingMail) && owner.getUsername().contains("@")) {
                this.billingMail = owner.getUsername();
            }
        }
        if (organisation.getCreated() != null) {
            this.created = organisation.getCreated().toLocalDate();
        }
        if (organisation.getReseller() != null) {
            this.reseller = organisation.getReseller();
        }
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public String getName() {
        return name;
    }

    public String getCc() {
        return cc;
    }

    public String getBillingMail() {
        return billingMail;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public CustomerUser getOwner() {
        return owner;
    }

    public LocalDate getCreated() {
        return created;
    }

    public String getReseller() {
        return reseller;
    }

    public boolean getHasClientCredentials() {
        return hasClientCredentials;
    }

    public void setHasClientCredentials(boolean hasClientCredentials) {
        this.hasClientCredentials = hasClientCredentials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Customer customer = (Customer) o;

        if (organisationId != null ? !organisationId.equals(customer.organisationId) : customer.organisationId != null)
            return false;
        if (name != null ? !name.equals(customer.name) : customer.name != null) return false;
        if (cc != null ? !cc.equals(customer.cc) : customer.cc != null) return false;
        if (billingMail != null ? !billingMail.equals(customer.billingMail) : customer.billingMail != null)
            return false;
        return (subscription != null ? subscription.equals(customer.subscription) : customer.subscription == null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (organisationId != null ? organisationId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (cc != null ? cc.hashCode() : 0);
        result = 31 * result + (billingMail != null ? billingMail.hashCode() : 0);
        result = 31 * result + (subscription != null ? subscription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "organisationId=" + organisationId +
                ", name='" + name + '\'' +
                ", cc='" + cc + '\'' +
                ", billingMail='" + billingMail + '\'' +
                ", subscription=" + subscription +
                '}';
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvited(boolean invited) {
        this.invited = invited;
    }

    public UserDTO getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UserDTO invitedBy) {
        this.invitedBy = invitedBy;
    }
}
