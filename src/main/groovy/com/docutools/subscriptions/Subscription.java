package com.docutools.subscriptions;

import com.docutools.users.Organisation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@ApiModel(value = "Subscription Resource")
public class Subscription {

    public static final int TEST_PERIOD_IN_DAYS = 30;

    @Id
    @Type(type = "pg-uuid")
    @ApiModelProperty(value = "Id of the Subscription")
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Type of the Subscription")
    private SubscriptionType type = SubscriptionType.Test;
    @OneToOne(optional = false)
    private Organisation organisation;
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Payment Plan of the Subscription")
    private PaymentPlan paymentPlan;
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Payment Type of the Subscription")
    private PaymentType paymentType;
    @Column(nullable = false)
    @ApiModelProperty(value = "When the subscription started")
    private LocalDate since;
    @ApiModelProperty(value = "When the subscription is going to end")
    private LocalDate until;
    @ApiModelProperty(value = "Whether there are postal bills or not")
    private boolean postalBills = false;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    @ApiModelProperty(value = "List of accounts in the subscription")
    private List<Account> accounts;

    protected Subscription() {
    }

    @JsonCreator
    public Subscription(@JsonProperty("type") SubscriptionType type,
                        @JsonProperty("paymentPlan") PaymentPlan paymentPlan,
                        @JsonProperty("paymentType") PaymentType paymentType,
                        @JsonProperty("postalBills") boolean postalBills,
                        @JsonProperty("until") LocalDate until) {
        this.type = type;
        this.paymentPlan = paymentPlan;
        this.paymentType = paymentType;
        this.postalBills = postalBills;
        this.until = until;
    }

    public Subscription(Organisation organisation) {
        Assert.notNull(organisation, "organisation is required - must not be NULL!");
        this.organisation = organisation;
        this.type = SubscriptionType.Test;
        this.accounts = new ArrayList<>();
        this.since = LocalDate.now();
        this.until = since.plusDays(TEST_PERIOD_IN_DAYS);
    }

    @JsonIgnore
    public UUID getId() {
        return id;
    }

    public SubscriptionType getType() {
        return type;
    }

    @JsonIgnore
    public Organisation getOrganisation() {
        return organisation;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getSince() {
        return since;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getUntil() {
        return until;
    }

    @JsonIgnore
    public List<Account> getAccounts() {
        return accounts;
    }

    public PaymentPlan getPaymentPlan() {
        return paymentPlan;
    }

    public void setPaymentPlan(PaymentPlan paymentPlan) {
        this.paymentPlan = paymentPlan;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isPostalBills() {
        return postalBills;
    }

    public void setPostalBills(boolean postalBills) {
        this.postalBills = postalBills;
    }

    @JsonIgnore
    public Optional<Account> newAccount() {
        if(type.hasAccountLimit() && accounts.size() >= type.getMaxAccounts()) {
            return Optional.empty();
        }
        Account account = new Account(this);
        accounts.add(account);
        return Optional.of(account);
    }

    @JsonIgnore
    public Optional<Account> getFreeAccount() {
        if(type == SubscriptionType.Master) {
            Optional<Account> free = accounts.stream()
                    .filter(Account::isUnassigned)
                    .findFirst();
            if(free.isPresent()) {
               return free;
            } else {
                return newAccount();
            }
        }
        return accounts.stream()
                .filter(Account::isUnassigned)
                .findFirst();
    }

    @JsonIgnore
    public void upgrade(SubscriptionType type, LocalDate until, PaymentType paymentType, PaymentPlan paymentPlan) {
        Assert.notNull(type, "type is required - must not be NULL!");
        if(type.isPaid()) {
            if(paymentType == null && this.paymentType == null)
                paymentType = PaymentType.Other;
            else if(paymentType != null) this.paymentType = paymentType;
            if(paymentPlan == null && this.paymentPlan == null)
                paymentPlan = PaymentPlan.OneTime;
            else if(paymentPlan != null) this.paymentPlan = paymentPlan;
        }
        this.type = type;
        this.until = until;
    }

    public void removeUntil() {
        this.until = null;
    }

    public boolean isActive() {
        return until == null || until.plusDays(1).isAfter(LocalDate.now());
    }

    @JsonProperty("availableAccounts")
    public long countAvailableAccounts() {
        return accounts.stream()
                .filter(Account::isActive)
                .filter(Account::isUnassigned)
                .count();
    }

    @JsonProperty("usedAccounts")
    public long countUsedAccounts() {
        return accounts.stream()
                .filter(Account::isActive)
                .filter(account -> !account.isUnassigned())
                .count();
    }

    @JsonProperty("totalAccounts")
    public long countActiveAccounts() {
        return accounts.stream()
                .filter(Account::isActive)
                .count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        if (postalBills != that.postalBills) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != that.type) return false;
        if (paymentPlan != that.paymentPlan) return false;
        if (paymentType != that.paymentType) return false;
        if (since != null ? !since.equals(that.since) : that.since != null) return false;
        return until != null ? until.equals(that.until) : that.until == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (paymentPlan != null ? paymentPlan.hashCode() : 0);
        result = 31 * result + (paymentType != null ? paymentType.hashCode() : 0);
        result = 31 * result + (since != null ? since.hashCode() : 0);
        result = 31 * result + (until != null ? until.hashCode() : 0);
        result = 31 * result + (postalBills ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", type=" + type +
                ", paymentPlan=" + paymentPlan +
                ", paymentType=" + paymentType +
                ", since=" + since +
                ", until=" + until +
                ", postalBills=" + postalBills +
                '}';
    }

}
