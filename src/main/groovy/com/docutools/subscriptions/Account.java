package com.docutools.subscriptions;

import com.docutools.users.DocutoolsUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@ApiModel(value = "Account Resource")
public class Account {

    @Id
    @Type(type = "pg-uuid")
    @ApiModelProperty(value = "Id of the Account")
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    @ApiModelProperty(value = "Timestamp of the accont activation")
    private LocalDate activated = LocalDate.now();
    @OneToOne
    private DocutoolsUser user;
    @ManyToOne(optional = false)
    private Subscription subscription;

    public Account() {
    }

    public Account(Subscription subscription) {
        Assert.notNull(subscription, "subscription is required - must not be NULL!");
        this.subscription = subscription;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getActivated() {
        return activated;
    }

    @JsonIgnore
    public DocutoolsUser getUser() {
        return user;
    }

    @JsonIgnore
    public Account assign(DocutoolsUser user) {
        Assert.isTrue(!subscription.getOrganisation().getId().equals(user.getId()),
                "user is invalid - must be from the same Organisation as Subscription!");
        this.user = user;
        this.activated = LocalDate.now();
        return this;
    }

    @JsonIgnore
    public Account removeAssignment() {
        this.user = null;
        return this;
    }

    public boolean isUnassigned() {
        return user == null;
    }

    @JsonIgnore
    public Subscription getSubscription() {
        return subscription;
    }

    public boolean isActive() {
        return subscription.isActive();
    }

    @JsonProperty("until")
    public LocalDate getActiveUntil() {
        return subscription.getUntil();
    }

    public String getType() {
        return subscription.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (id != null ? !id.equals(account.id) : account.id != null) return false;
        return activated != null ? activated.equals(account.activated) : account.activated == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (activated != null ? activated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", activated=" + activated +
                '}';
    }
}
