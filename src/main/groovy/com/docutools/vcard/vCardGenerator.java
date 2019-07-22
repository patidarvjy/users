package com.docutools.vcard;

import com.docutools.avatar.AvatarService;
import com.docutools.contacts.ProjectContact;
import com.docutools.users.DocutoolsUser;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;

public class vCardGenerator {
    public static String generateVCard(VCardVersion version, vCardData data){
        VCard vCard = new VCard(version);

        if(data.getStructuredName() != null) {
            vCard.setStructuredName(data.getStructuredName());
        }

        if(data.getEmail() != null) {
            vCard.addEmail(data.getEmail(), EmailType.WORK);
        }

        if(data.getPhone() != null){
            vCard.addTelephoneNumber(data.getPhone(), TelephoneType.CELL);
        }

        if(data.getFax() != null){
            vCard.addTelephoneNumber(data.getFax(), TelephoneType.FAX);
        }

        if(data.getAddress() != null){
            vCard.addAddress(data.getAddress());
        }

        if(data.getOrganization() != null){
            vCard.setOrganization(data.getOrganization());
        }

        if(data.getPhoto() != null){
            vCard.addPhoto(data.getPhoto());
        }

        return Ezvcard.write(vCard)
                .version(version)
                .go();
    }

    public static String generateVCard(VCardVersion version, ProjectContact contact){
        return generateVCard(version, new vCardData(contact));
    }

    public static String generateVCard(VCardVersion version, DocutoolsUser user, AvatarService avatarService){
        return generateVCard(version, new vCardData(user, avatarService));
    }
}
