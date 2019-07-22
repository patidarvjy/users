package com.docutools.vcard;

import com.docutools.avatar.AvatarService;
import com.docutools.contacts.ProjectContact;
import com.docutools.users.DocutoolsUser;
import ezvcard.parameter.ImageType;
import ezvcard.property.Address;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.StructuredName;

public class vCardData {
    private StructuredName structuredName;
    private String email;
    private String phone;
    private String fax;
    private Address address;
    private Organization organization;
    private Photo photo;

    public vCardData(ProjectContact contact){
        StructuredName structuredName = new StructuredName();
        if(contact.getFirstName() != null){
            structuredName.setGiven(contact.getFirstName());
        }

        if(contact.getLastName() != null){
            structuredName.setFamily(contact.getLastName());
        }

        this.structuredName = structuredName;

        if(contact.getEmail() != null){
            this.email = contact.getEmail();
        }
        if(contact.getPhone() != null){
            this.phone = contact.getPhone();
        }
        if(contact.getFax() != null){
            this.fax = contact.getFax();
        }

        if(contact.getStreet() != null || contact.getCity() != null || contact.getZip() != null){
            Address address = new Address();
            if(contact.getStreet() != null){
                address.setStreetAddress(contact.getStreet());
            }
            if(contact.getCity() != null){
                address.setLocality(contact.getCity());
            }
            if(contact.getZip() != null){
                address.setPostalCode(contact.getZip());
            }
            this.address = address;
        }

        if(contact.getCompanyName() != null || contact.getDepartment() != null){
            Organization organization = new Organization();
            if(contact.getCompanyName() != null){
                organization.getValues().add(contact.getCompanyName());
            }
            if(contact.getDepartment() != null){
                organization.getValues().add(contact.getDepartment());
            }
            this.organization = organization;
        }
    }

    public vCardData(DocutoolsUser user, AvatarService avatarService){
        if(user.getName() != null){
            StructuredName structuredName = new StructuredName();
            if(user.getName().getFirstName() != null){
                structuredName.setGiven(user.getName().getFirstName());
            }
            if(user.getName().getLastName() != null){
                structuredName.setFamily(user.getName().getLastName());
            }
            if(user.getSettings().getLanguage() != null){
                structuredName.setLanguage(user.getSettings().getLanguage());
            }
            this.structuredName = structuredName;
        }

        if(user.getEmail() != null){
            this.email = user.getEmail();
        } else if (user.getUsername().contains("@")){
            this.email = user.getUsername();
        }

        if(user.getPhone() != null){
            this.phone = user.getPhone();
        }

        if(user.getFax() != null){
            this.fax = user.getFax();
        }

        if(user.getStreet() != null || user.getCity() != null || user.getZip() != null){
            Address address = new Address();
            if(user.getStreet() != null){
                address.setStreetAddress(user.getStreet());
            }
            if(user.getCity() != null){
                address.setLocality(user.getCity());
            }
            if(user.getZip() != null){
                address.setPostalCode(user.getZip());
            }
            this.address = address;
        }

        if(user.getOrganisation() != null || user.getDepartment() != null) {
            Organization organization = new Organization();
            if(user.getOrganisation().getName() != null){
                organization.getValues().add(user.getOrganisation().getName());
            }
            if(user.getDepartment() != null){
                organization.getValues().add(user.getDepartment());
            }
            this.organization = organization;
        }

        avatarService.getAvatarData(user.getId())
                .ifPresent(data -> {
                    this.photo = new Photo(data, ImageType.JPEG);
                });

    }

    public StructuredName getStructuredName() {
        return structuredName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getFax() {
        return fax;
    }

    public Address getAddress() {
        return address;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Photo getPhoto() {
        return photo;
    }
}
