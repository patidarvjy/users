package com.docutools.users.values

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * An {@link com.docutools.users.Organisation}s tax number. If the organisation is european, it also indicates if
 * the number is valid or not.
 */
@Embeddable
@ApiModel(value = "Vat Number Resource")
class VatNumber {

    @ApiModelProperty(value = "The Tax Number")
    @Column(name = "vat_number", length = 32) String number
    @ApiModelProperty(value = "Whether it is valid or not")
    @Column(name = "vat_valid") Boolean valid

    VatNumber() {
    }

    VatNumber(String number) {
        this.number = number
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        VatNumber vatNumber = (VatNumber) o

        if (number != vatNumber.number) return false
        if (valid != vatNumber.valid) return false

        return true
    }

    int hashCode() {
        int result
        result = (number != null ? number.hashCode() : 0)
        result = 31 * result + (valid != null ? valid.hashCode() : 0)
        return result
    }

    String toString() {
        "VAT: <${number}>"
    }
}
