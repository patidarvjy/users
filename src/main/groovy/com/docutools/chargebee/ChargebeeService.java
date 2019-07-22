package com.docutools.chargebee;

import com.chargebee.models.Customer;
import com.chargebee.models.Event;
import com.chargebee.org.json.JSONObject;
import com.docutools.customers.SubscriptionUpdate;
import com.docutools.exceptions.ExceptionHelper;
import com.docutools.subscriptions.PaymentPlan;
import com.docutools.subscriptions.Subscription;
import com.docutools.subscriptions.SubscriptionType;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChargebeeService {

    private static final Boolean TRUE_TO_PREVENT_RETRIES = Boolean.TRUE;
    private final ChargebeeApiClient apiClient;

    private final SessionManager sessionManager;


    private static final Logger log = LoggerFactory.getLogger(ChargebeeService.class);

    private final OrganisationRepo organisationRepo;

    @Autowired
    public ChargebeeService(SessionManager sessionManager,
                            OrganisationRepo organisationRepo,
                            ChargebeeApiClient apiClient) {
        this.sessionManager = sessionManager;
        this.organisationRepo = organisationRepo;
        this.apiClient = apiClient;
    }

    public Optional<JSONObject> getCheckoutPage(String planId) {
        Assert.notNull(planId, "planId is required - must not be NULL!");
        Organisation organisation = sessionManager.getCurrentUser().getOrganisation();
        try {
            JSONObject hostedPage = apiClient.getPlanCheckoutForOrganisation(planId, organisation);
            return Optional.of(hostedPage);
        } catch (Exception e) {
            log.error(String.format("could not create checkout for organisation %s and plan %s", organisation.getId(), planId));
            log.error(e.getMessage());
            throw ExceptionHelper.newInternalServerError("error creating checkout", e);
        }
    }

    public Optional<JSONObject> getPortal() {
        Organisation organisation = sessionManager.getCurrentUser().getOrganisation();
        try {
            JSONObject toReturn = apiClient.getPortalForOrganisation(organisation);
            return Optional.of(toReturn);
        } catch (Exception e) {
            log.error(String.format("could not create portal for organisation %s", organisation.getId()));
            log.error(e.getMessage());
            throw ExceptionHelper.newInternalServerError("error creating portal", e);
        }
    }

    public Optional<Boolean> processEvent(String jsonEvent) {
        try {
            Event incomingEvent = new Event(jsonEvent);
            //Retrieve event from chargebee to prevent impersonation
            Event event = apiClient.getEventById(incomingEvent.id());
            switch (event.eventType()) {
                case CUSTOMER_CREATED:
                    Organisation organisation = getOrganisationFromEvent(event);
                    setHasChargebee(organisation);
                    String reseller = getResellerFromEvent(event);
                    setReseller(reseller, organisation);
                    break;
                case CUSTOMER_DELETED:
                    organisation = getOrganisationFromEvent(event);
                    unsetHasChargebee(organisation);
                    unsetReseller(organisation);
                    cancelSubscription(organisation);
                    break;
                case SUBSCRIPTION_CREATED:
                    organisation = getOrganisationFromEvent(event);
                    createSubscription(organisation, event.content().subscription());
                    break;
                case SUBSCRIPTION_CANCELLED:
                    organisation = getOrganisationFromEvent(event);
                    cancelSubscription(organisation);
                    break;
                case SUBSCRIPTION_CHANGED:
                    organisation = getOrganisationFromEvent(event);
                    updateSubscription(organisation, event.content().subscription());
                    break;
                case SUBSCRIPTION_RENEWED:
                    //TODO
                case SUBSCRIPTION_DELETED:
                case SUBSCRIPTION_PAUSED:
                case SUBSCRIPTION_RESUMED:
                default:
                    log.error("Unhandled operation {}", event.eventType());
//                    log.error(event.toString());
                    break;
            }

        } catch (Exception e) {
            log.error("Error occured while handling an event", e);
            return Optional.of(TRUE_TO_PREVENT_RETRIES);
        }
        return Optional.of(TRUE_TO_PREVENT_RETRIES);
    }

    private String getResellerFromEvent(Event event) {
        Customer customer = event.content().customer();
        return customer.optString("cf_salespartner");
    }

    private void setReseller(String reseller, Organisation organisation) {
        organisation.setReseller(reseller);
        organisationRepo.save(organisation);
    }

    private void unsetReseller(Organisation organisation) {
        organisation.setReseller("");
        organisationRepo.save(organisation);
    }

    private void updateSubscription(Organisation organisation, com.chargebee.models.Subscription subscription) {
        log.info("Organisation " + organisation.getId() + " changed subscription to " + subscription.planId());
        SubscriptionUpdate subscriptionUpdate = createDocutoolsSubscriptionUpdateFromChargebeeSubscription(subscription);
        Subscription currentSubscription = organisation.getSubscription();
        subscriptionUpdate.apply(currentSubscription);
        organisationRepo.save(organisation);
    }

    private void cancelSubscription(Organisation organisation) {
        log.info("Organisation " + organisation.getId() + " canceled subscription");
        SubscriptionUpdate subscriptionUpdate = getDefaultSubscription(organisation);
        Subscription currentSubscription = organisation.getSubscription();
        subscriptionUpdate.apply(currentSubscription);
        organisationRepo.save(organisation);
    }

    private SubscriptionUpdate getDefaultSubscription(Organisation organisation) {
        Assert.notNull(organisation, "organisation is required - must not be NULL!");
        return new SubscriptionUpdate(SubscriptionType.Test,
                null,
                null,
                false,
                LocalDate.now().plusDays(Subscription.TEST_PERIOD_IN_DAYS),
                false);
    }

    private void createSubscription(Organisation organisation, com.chargebee.models.Subscription subscription) {
        log.info("Organisation " + organisation.getId() + " subscribed to " + subscription.planId());
        SubscriptionUpdate subscriptionUpdate = createDocutoolsSubscriptionUpdateFromChargebeeSubscription(subscription);
        Subscription currentSubscription = organisation.getSubscription();
        subscriptionUpdate.apply(currentSubscription);
        organisationRepo.save(organisation);
    }

    private void unsetHasChargebee(Organisation organisation) {
        log.info("Organisation " + organisation.getId() + " unregistered from Chargebee");
        organisation.setHasChargebeeAccount(false);
        organisationRepo.save(organisation);
    }

    private void setHasChargebee(Organisation organisation) {
        log.info("Organisation " + organisation.getId() + " registered for Chargebee");
        organisation.setHasChargebeeAccount(true);
        organisationRepo.save(organisation);
    }

    private Organisation getOrganisationFromEvent(Event event) {
        return organisationRepo
                .findById(UUID
                        .fromString(event.content()
                                .customer()
                                .id()))
                .get();
    }

    private SubscriptionUpdate createDocutoolsSubscriptionUpdateFromChargebeeSubscription(com.chargebee.models.Subscription chargebeeSubscription) {
        SubscriptionType subscriptionType = null;
        PaymentPlan paymentPlan = null;
        switch (ChargebeePlan.getEnum(chargebeeSubscription.planId())) {
            case POCKET_TOOL_MONTHLY:
                subscriptionType = SubscriptionType.Pocket;
                paymentPlan = PaymentPlan.Monthly;
                break;
            case COMBO_TOOL_MONTHLY:
                subscriptionType = SubscriptionType.Combo;
                paymentPlan = PaymentPlan.Monthly;
                break;
            case MULTI_TOOL_MONTHLY:
                subscriptionType = SubscriptionType.Multi;
                paymentPlan = PaymentPlan.Monthly;
                break;
            case POCKET_TOOL_YEARLY:
                subscriptionType = SubscriptionType.Pocket;
                paymentPlan = PaymentPlan.Annually;
                break;
            case COMBO_TOOL_YEARLY:
                subscriptionType = SubscriptionType.Combo;
                paymentPlan = PaymentPlan.Annually;
                break;
            case MULTI_TOOL_YEARLY:
                subscriptionType = SubscriptionType.Multi;
                paymentPlan = PaymentPlan.Annually;
                break;
            default:
                log.error("Unhandled plan {}", chargebeeSubscription.planId());
                break;
        }
        return SubscriptionUpdate.newContinousSubscription(subscriptionType, paymentPlan);
    }
}
