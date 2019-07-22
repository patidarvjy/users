package com.docutools.saml;

import com.docutools.users.Organisation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * Request from a SAML Service Provider to auhorize a user and return a JWT/Bearer token.
 *
 * The service provider will send the user's email address and entityID of the SAML IdP. The organisation this user is
 * or will belong too is linked with the given IdP entityID.
 *
 * If the user does not exist in our database create a profile for him.
 *
 * The request will be verified by concatenating email and idp in the following way:
 * "idp +++ email" against the signature in the sign field.
 *
 * @author amp, munish
 */
public class SAMLAuthenticationRequest implements Serializable{

    private String username;
    private String email;
    private String idp;
    private String sign;
    private String firstName;
    private String lastName;
    private String language;
    private String company;

    /**
     * Used to deserialize from JSON.
     *
     * @param email email address of the user
     * @param idp SAML Entitz ID of the IDPs metadata
     * @param sign signature of email.idp
     */
    @JsonCreator
    public SAMLAuthenticationRequest(@JsonProperty(value = "username") String username,
                                     @JsonProperty(value = "email", required = true) String email,
                                     @JsonProperty(value = "idp", required = true) String idp,
                                     @JsonProperty(value = "sign", required = true) String sign,
                                     @JsonProperty(value = "firstName") String firstName,
                                     @JsonProperty(value = "lastName") String lastName,
                                     @JsonProperty(value = "language") String language,
                                     @JsonProperty(value = "company") String company) {
        this.username = username;
        this.email = email;
        this.idp = idp;
        this.sign = sign;
        this.firstName = firstName;
        this.lastName = lastName;
        this.language = language;
        this.company = company;
    }

    public String getUsername() {
        if(StringUtils.isEmpty(username))
            return email;
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getIdp() {
        return idp;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSign() {
        return sign;
    }

    public String getLanguage() {
        return language;
    }

    public String getCompany() {
        return company;
    }

    @JsonIgnore
    public boolean sameIdPAs(Organisation organisation) {
        return !StringUtils.isEmpty(this.idp) && this.idp.equals(organisation.getIdpLink());
    }

    @JsonIgnore
    public String toSignatureMessage() {
        return String.format("%s +++ %s", email, idp);
    }

    @Override
    public String toString() {
        return String.format("SAML Authentication Request [email=%s, idp=%s, sign=%s]", email, idp, sign);
    }

}
