package com.docutools.users.resources

import com.docutools.subscriptions.Subscription
import com.docutools.users.DocutoolsUser
import com.docutools.users.Organisation
import com.docutools.users.values.VatNumber
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.util.StringUtils

/**
 * Resource representation of {@link Organisation}.
 */
@ApiModel(value = "Organisation Resource")
class OrganisationDTO extends AuditedDTO{

    @ApiModelProperty(value = "Id of the Organisation")
    UUID id
    @ApiModelProperty(value = "Name of the Organisation")
    String name
    VatNumberDTO vat
    @ApiModelProperty(value = "Country Code of the Organisation")
    String cc
    @ApiModelProperty(value = "Billing Mail of the Organisation")
    String billingMail
    Subscription subscription
    @ApiModelProperty(value = "IPDLink of the Organisation")
    String idpLink
    @ApiModelProperty(value = "Map of String to String for Messages that should be shown if no License exists")
    Map<String, String> noLicenseMessages
    @ApiModelProperty(value = "If organisation already has a chargebee account")
    boolean hasChargebeeAccount

    OrganisationDTO(Organisation entity, DocutoolsUser user) {
        super(entity)
        id = entity.id
        name = entity.name
        if(entity.vat) vat = new VatNumberDTO(entity.vat)
        cc = entity.cc
        subscription = entity.subscription
        if(StringUtils.isEmpty(entity.billingMail)) {
            this.billingMail = entity?.owner?.username
        } else {
            this.billingMail = entity.billingMail
        }
        if (user.isOrganisationOwner()) idpLink = entity.idpLink
        noLicenseMessages = entity.noLicenseMessages
        hasChargebeeAccount = entity.getHasChargebeeAccount()
    }

    OrganisationDTO() {
    }

    static class VatNumberDTO {
        @ApiModelProperty(value = "Vat Number")
        String number
        @ApiModelProperty(value = "Whether Vat Number is valid or not")
        Boolean valid

        VatNumberDTO(VatNumber entity) {
            number = entity?.number
            valid = entity?.valid
        }

        VatNumberDTO() {
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            VatNumberDTO that = (VatNumberDTO) o

            if (number != that.number) return false
            if (valid != that.valid) return false

            return true
        }

        int hashCode() {
            int result
            result = (number != null ? number.hashCode() : 0)
            result = 31 * result + (valid != null ? valid.hashCode() : 0)
            return result
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        OrganisationDTO that = (OrganisationDTO) o

        if (cc != that.cc) return false
        if (id != that.id) return false
        if (name != that.name) return false
        if (vat != that.vat) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (vat != null ? vat.hashCode() : 0)
        result = 31 * result + (cc != null ? cc.hashCode() : 0)
        return result
    }
}
