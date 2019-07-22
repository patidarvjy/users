package com.docutools.customers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewCustomerUser extends CustomerUser {

    @JsonCreator
    public NewCustomerUser(@JsonProperty(value = "email", required = true) String email) {
        super(null, email);
    }

}
