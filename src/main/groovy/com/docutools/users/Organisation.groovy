package com.docutools.users

import com.docutools.config.jpa.auditing.AuditedEntity
import com.docutools.subscriptions.Subscription
import com.docutools.users.values.VatNumber
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * An {@link Organisation} represents a company who pays for one or more docutools users. These users share mostly the
 * same projects and are managed by other users in the organisation.
 */
@Entity
@Table(name = 'organisations')
@ApiModel(value = "Organisation Resource")
class Organisation extends AuditedEntity {

    static final String PLAN_UPGRADE_NOT_ALLOWED = 'PLAN_UPGRADE_NOT_ALLOWED'

    @Id
    @Type(type = "pg-uuid") UUID id = UUID.randomUUID()
    @Column(length = 128)
    @ApiModelProperty(value = "Name of the Organisation")
    String name = ''
    @Embedded VatNumber vat = new VatNumber()
    @Column(length = 2)
    @ApiModelProperty(value = "Country code of the Organisation")
    String cc
    @Column(name = 'billing_email', nullable = false)
    @ApiModelProperty(value = "Billing Mail of the Organisation")
    String billingMail

    @OneToOne(mappedBy = "organisation", optional = false, cascade = CascadeType.ALL)
    private Subscription subscription = new Subscription(this)
    @OneToOne
    DocutoolsUser owner
    @OneToMany(mappedBy = "organisation")
    @ApiModelProperty(value = "List of all Users in the organisation")
    List<DocutoolsUser> members
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ApiModelProperty(value = "List of all organisation names")
    List<OrganisationName> names = new ArrayList<>()

    @ApiModelProperty(value = "IDPLink of the organisation")
    String idpLink

    @ElementCollection
    @JoinTable(name="no_license_messages", joinColumns=@JoinColumn(name="org_id"))
    @MapKeyColumn (name="lang")
    @Column(name="message")
    @ApiModelProperty(value = "Map of language codes to messages, shown when an organisation user has no license")
    private Map<String, String> noLicenseMessages = new HashMap<>()

    @ApiModelProperty(value = "Whether the organisation already has a chargebee account")
    Boolean hasChargebeeAccount

    @ApiModelProperty(value = "The reseller of the organisation")
    String reseller

    @Column(nullable = true)
    String passwordPolicy

    @ApiModelProperty(value = "If true, the users of this organization mus have SMS/MFA enabled.")
    @Column(nullable = false)
    boolean forceMfa = false

    Organisation() {
        billingMail = ""
    }

    Subscription getSubscription() {
        if(subscription == null) {
            subscription = new Subscription(this)
        }
        subscription
    }

    void setSubscription(Subscription subscription) {
        this.subscription = subscription
    }

    OrganisationName newName(String name) {
        def organisationName = new OrganisationName(name, this)
        names.add(organisationName)
        return organisationName
    }

    void removeName(String name){
        names.stream()
            .filter({ (it.name == name) })
            .findFirst()
            .ifPresent { n ->
            names.remove(n)
        }
    }

    Map<String, String> getNoLicenseMessages() {
        return noLicenseMessages
    }

    void setNoLicenseMessages(Map<String, String> noLicenseMessages) {
        this.noLicenseMessages = noLicenseMessages
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Organisation that = (Organisation) o

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

    String toString() {
        "Organisation<${id}:${name}>"
    }
}
