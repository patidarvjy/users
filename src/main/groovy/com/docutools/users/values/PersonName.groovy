package com.docutools.users.values

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * Value object for the first and last name of a person.
 */
@Embeddable
@ApiModel(value = "Person Name Resource")
class PersonName {

    @Column(nullable = false)
    @ApiModelProperty(value = "First Name of the Person")
    String firstName = ''
    @ApiModelProperty(value = "Last Name of the Person")
    @Column(nullable = false)
    String lastName = ''

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        PersonName that = (PersonName) o

        if (firstName != that.firstName) return false
        if (lastName != that.lastName) return false

        return true
    }

    int hashCode() {
        int result
        result = (firstName != null ? firstName.hashCode() : 0)
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0)
        return result
    }

    String toString() {
        "${firstName} ${lastName}"
    }
}
