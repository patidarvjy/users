package com.docutools.customers;

import com.docutools.subscriptions.PaymentPlan;
import com.docutools.subscriptions.PaymentType;
import com.docutools.subscriptions.Subscription;
import com.docutools.subscriptions.SubscriptionType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class SubscriptionUpdate extends Subscription {

    /**
     * Used as arbitrary default value in {@link this#newContinousSubscription(SubscriptionType, PaymentPlan)}.
     */
    private static final LocalDate DEFAULT_UNTIL = LocalDate.of(2200, 12, 31);

    /**
     * Creates a new {@link SubscriptionUpdate} for users who have a continous subscription with yearly credit card
     * payments.
     *
     * @param subscriptionType {@link SubscriptionType}
     * @param paymentPlan {@link PaymentPlan}
     * @return the new {@link SubscriptionUpdate}
     */
    public static SubscriptionUpdate newContinousSubscription(SubscriptionType subscriptionType, PaymentPlan paymentPlan) {
        return new SubscriptionUpdate(subscriptionType,
                paymentPlan,
                PaymentType.CreditCard,
                false,
                DEFAULT_UNTIL,
                true);
    }

    private Boolean postalBills;
    private boolean removeUntil;

    @JsonCreator
    public SubscriptionUpdate(@JsonProperty("type") SubscriptionType type,
                              @JsonProperty("paymentPlan") PaymentPlan paymentPlan,
                              @JsonProperty("paymentType") PaymentType paymentType,
                              @JsonProperty("postalBills") Boolean postalBills,
                              @JsonProperty("until")LocalDate until,
                              @JsonProperty(value = "removeUntil", defaultValue = "false") boolean removeUntil) {
        super(type, paymentPlan, paymentType, false, until);
        this.postalBills = postalBills;
        this.removeUntil = removeUntil;
    }

    public boolean hasChanges() {
        return getType() != null || getPaymentPlan() != null || getPaymentType() != null || postalBills != null || getUntil() != null || removeUntil;
    }

    public Subscription apply(Subscription subscription) {
        SubscriptionType type = getType() != null? getType() : subscription.getType();
        PaymentPlan paymentPlan = getPaymentPlan() != null? getPaymentPlan() : subscription.getPaymentPlan();
        PaymentType paymentType = getPaymentType() != null? getPaymentType() : subscription.getPaymentType();
        LocalDate until = getUntil() != null? getUntil() : subscription.getUntil();
        subscription.upgrade(type, until, paymentType, paymentPlan);

        boolean postalBills = this.postalBills != null? this.postalBills : subscription.isPostalBills();
        subscription.setPostalBills(postalBills);

        if(removeUntil) {
            subscription.removeUntil();
        }

        return subscription;
    }

}
