package com.docutools.chargebee;

import com.chargebee.Environment;
import com.chargebee.Result;
import com.chargebee.models.Event;
import com.chargebee.models.HostedPage;
import com.chargebee.models.PortalSession;
import com.chargebee.org.json.JSONObject;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This Class is responsible for the communcation with the chargebee service
 */
@Component
public class ChargebeeApiClient {

    private final boolean enabled;
    private static final Logger log = LoggerFactory.getLogger(ChargebeeApiClient.class);

    public ChargebeeApiClient(@Value("${docutools.chargebee.site-name:}") String chargebeeSiteName,
                              @Value("${docutools.chargebee.api-key-path:}") String chargebeeApiKeyPath,
                              @Value("${docutools.chargebee.enabled:false}") boolean chargebeeEnabled) throws NoChargebeeConfigException {
        this.enabled = chargebeeEnabled;
        if (chargebeeEnabled) {
            if (chargebeeSiteName == null || chargebeeApiKeyPath == null) {
                throw new NoChargebeeConfigException();
            }
            String chargebeeApiKey = FileUtils.readSecret(chargebeeApiKeyPath);
            Environment.configure(chargebeeSiteName, chargebeeApiKey);
        }
    }

    private void requireEnabled() {
        if(!enabled) {
            throw new NoChargebeeConfigException("Chargebee is not enabled in this environment!");
        }
    }

    /**
     * This method is used to retrieve events from the chargebee servers
     * https://www.chargebee.com/docs/events_and_webhooks.html
     * https://apidocs.chargebee.com/docs/api/events
     *
     * @param id The event to retrieve from the server
     * @return the {@link Event}
     * @throws Exception If there happens something in the chargebee API during event retrieval
     * @throws NoChargebeeConfigException when the Chargebee Integration is not enabled for this feature.
     */
    public Event getEventById(String id) throws Exception {
        requireEnabled();
        Result result = Event.retrieve(id)
                .request();
        return result.event();
    }


    /**
     * This method is responsible for creating a chargebee hosted checkout page for customers who
     * want to buy a plan.
     * It should only be called if the organisation does not have a chargebee account yet
     * https://www.chargebee.com/checkout-portal-docs/api-checkout.html
     * https://apidocs.chargebee.com/docs/api/hosted_pages#checkout_new_subscription
     *
     * @param planId       The id of the plan the customer wants to buy
     * @param organisation The id of the organisation the interacting customer is part of
     * @return A {@link JSONObject} containing the hosted page
     * @throws Exception If there happens something in the chargebee API during checkout creation
     */
    public JSONObject getPlanCheckoutForOrganisation(String planId, Organisation organisation) throws Exception {
        requireEnabled();
        Result result = getCheckout(planId, organisation);
        return result.jsonResponse().getJSONObject("hosted_page");
    }

    private Result getCheckout(String planId, Organisation organisation) throws Exception {
        HostedPage.CheckoutNewRequest checkoutNewRequest = createCheckoutRequest(planId, organisation);
        return checkoutNewRequest.request();
    }

    private HostedPage.CheckoutNewRequest createCheckoutRequest(String planId, Organisation organisation) throws IOException {
        String organisationId = organisation.getId().toString();
        DocutoolsUser owner = organisation.getOwner();
        HostedPage.CheckoutNewRequest checkoutNewRequest = HostedPage.checkoutNew()
                .subscriptionPlanId(planId)
                .customerId(organisationId)
                .subscriptionId(organisationId)
                .customerCompany(organisation.getName())
                .billingAddressCountry(organisation.getCc().toUpperCase());

        if (organisation.getVat() != null) {
            checkoutNewRequest.customerVatNumber(organisation.getVat().getNumber());
        }

        if (owner.getSettings() != null && owner.getSettings().getLanguage() != null) {
            checkoutNewRequest.customerLocale(owner.getSettings().getLanguage());
        } else {
            checkoutNewRequest.customerLocale("en");
        }

        if (organisation.getBillingMail() != null) {
            checkoutNewRequest.customerEmail(organisation.getBillingMail());
        } else if (owner.getEmail() != null) {
            checkoutNewRequest.customerEmail(owner.getEmail());
        } else {
            checkoutNewRequest.customerEmail(owner.getUsername());
        }

        if (organisation.getCc() != null) {
            checkoutNewRequest.billingAddressCountry(organisation.getCc().toUpperCase());
        } else {
            checkoutNewRequest.billingAddressCountry("AT");
        }

        return checkoutNewRequest;
    }

    /**
     * This method is responsible for creating a chargebee portal for already signed up organisations.
     * In the portal they can edit their subscription details (change plan, no of licenses, ...)
     * This method should only be called when the organisation does already have a chargebee account
     * https://www.chargebee.com/checkout-portal-docs/api-portal.html
     * https://apidocs.chargebee.com/docs/api/portal_sessions
     *
     * @param organisation The organisation to get the portal for
     * @return A {@link JSONObject} containing the portal hosted page
     * @throws Exception If there happens something in the chargebee API during portal creation
     */
    public JSONObject getPortalForOrganisation(Organisation organisation) throws Exception {
        requireEnabled();
        Result result = PortalSession.create()
                .customerId(organisation.getId().toString())
                .request();
        return result.jsonResponse().getJSONObject("portal_session");
    }
}
