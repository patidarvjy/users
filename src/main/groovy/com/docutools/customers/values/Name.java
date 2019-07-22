package com.docutools.customers.values;

import com.docutools.users.values.PersonName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.Assert;

@ApiModel(value = "Person Name Resource")
public class Name {

    @ApiModelProperty(value = "First Name Of the Person")
    private String firstName;
    @ApiModelProperty(value = "Last Name of the Person")
    private String lastName;

    public Name(PersonName personName) {
        Assert.notNull(personName, "personName is required - must not be NULL!");
        this.firstName = personName.getFirstName();
        this.lastName = personName.getLastName();
    }

    @JsonCreator
    public Name(@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name = (Name) o;

        if (firstName != null ? !firstName.equals(name.firstName) : name.firstName != null) return false;
        return lastName != null ? lastName.equals(name.lastName) : name.lastName == null;
    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }
}
