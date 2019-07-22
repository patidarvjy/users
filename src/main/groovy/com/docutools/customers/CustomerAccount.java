package com.docutools.customers;

import com.docutools.subscriptions.Account;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDate;
import java.util.UUID;

@ApiModel(value = "Customer Account Resource")
public class CustomerAccount {

    @ApiModelProperty(value = "Id of the Customer Account")
    private UUID id;
    @ApiModelProperty(value = "Whether the Customer Account is free or not")
    private boolean free;
    private AccountHolder holder;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "Whether the account is activated or not")
    private LocalDate activated;

    public CustomerAccount(Account account) {
        this.id = account.getId();
        this.free = account.isUnassigned();
        if(!free) {
            this.holder = new AccountHolder(account.getUser());
        }
        this.activated = account.getActivated();
    }

    public UUID getId() {
        return id;
    }

    public boolean isFree() {
        return free;
    }

    public AccountHolder getHolder() {
        return holder;
    }

    public LocalDate getActivated() {
        return activated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerAccount that = (CustomerAccount) o;

        if (free != that.free) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (holder != null ? !holder.equals(that.holder) : that.holder != null) return false;
        return activated != null ? activated.equals(that.activated) : that.activated == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (free ? 1 : 0);
        result = 31 * result + (holder != null ? holder.hashCode() : 0);
        result = 31 * result + (activated != null ? activated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CustomerAccount{" +
                "id=" + id +
                ", free=" + free +
                ", holder=" + holder +
                ", activated=" + activated +
                '}';
    }

    public void generateActivationLink(String link) {
        if (holder != null) {
            holder.generateActivationLink(link);
        }
    }

    public String getActivationLink() {
        return holder != null ? holder.getActivationLink() : "";
    }
}
