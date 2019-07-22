package com.docutools.customers;

import com.docutools.exceptions.ExceptionHelper;
import com.docutools.exceptions.ErrorCodes;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.UUID;

@ApiModel(value = "Customer Update Resource")
public class CustomerUpdate extends Customer {

    @ApiModelProperty(value = "Id of the organisation of the Customer Update")
    private UUID organisationId;

    @JsonCreator
    public CustomerUpdate(@JsonProperty("name") String name,
                          @JsonProperty("cc") String cc,
                          @JsonProperty("billingMail") String billingMail,
                          @JsonProperty("subscription") SubscriptionUpdate subscription,
                          @JsonProperty("owner") CustomerUser owner) {
        super(name, cc, billingMail, subscription, owner);
    }

    @Override
    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        Assert.notNull(organisationId, "organisationId is required - must not be NULL!");
        this.organisationId = organisationId;
    }

    public boolean hasChanges() {
        return !StringUtils.isEmpty(getName()) || !StringUtils.isEmpty(getCc()) ||
                getBillingMail() != null ||
                (getSubscription() != null && ((SubscriptionUpdate)getSubscription()).hasChanges()) ||
                (getOwner() != null && getOwner().getUserId() != null);
    }

    public Organisation apply(Organisation organisation) {
        if(!hasChanges()) {
            return organisation;
        }
        if(!StringUtils.isEmpty(getName())) {
            organisation.setName(getName());
        }
        if(!StringUtils.isEmpty(getCc())) {
            organisation.setCc(getCc());
        }
        if(getBillingMail() != null) {
            organisation.setBillingMail(getBillingMail());
        }
        if(getSubscription() != null) {
            ((SubscriptionUpdate)getSubscription()).apply(organisation.getSubscription());
        }
        CustomerUser owner = getOwner();
        if(owner != null && owner.getUserId() != null) {
            DocutoolsUser newOwner = organisation.getMembers().stream()
                    .filter(docutoolsUser -> docutoolsUser.getId().equals(owner.getUserId()))
                    .findFirst().orElseThrow(() -> ExceptionHelper.newBadRequestError(ErrorCodes.USER_NOT_FOUND));
            organisation.setOwner(newOwner);
        }
        return organisation;
    }

}
