package com.docutools.emails.mailservers;

import com.docutools.emails.EmailTemplateType;
import com.docutools.users.DocutoolsUser;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Personalization;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SendGridTemplate {

    private EmailTemplateType type;
    private Map<String, String> tempalteIds;
    private String[] categories;

    public SendGridTemplate(EmailTemplateType templateType, String[] categories) {
        this.type = templateType;
        this.categories = categories;
        tempalteIds = new HashMap<>();
    }

    public EmailTemplateType getType() {
        return type;
    }

    public void put(String lang, String id) {
        tempalteIds.put(lang, id);
    }

    public String getId(String lang) {
        return tempalteIds.get(lang);
    }

    public boolean supports(String lang) {
        return tempalteIds.containsKey(lang);
    }

    public Mail toMail(DocutoolsUser user, String url, Map<String, Object> properties) {
        String userEmail = user.getEmail();
        if(StringUtils.isEmpty(userEmail) || !userEmail.contains("@")) {
            userEmail = user.getUsername();
        }
        Mail mail = new Mail();
        Arrays.stream(categories).forEach(mail::addCategory);
        mail.setTemplateId(tempalteIds.getOrDefault(user.getSettings().getLanguage(), tempalteIds.get("en")));
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(userEmail, user.getName().toString()));

        if (properties != null) {
            properties.forEach((key, value) -> personalization.addSubstitution(key, value.toString()));
        }
        personalization.addSubstitution("-url-", url);
        personalization.addSubstitution("user_name", user.getName().toString());
        personalization.addSubstitution("-token-", user.getVerificationStatus().getToken());
        mail.addPersonalization(personalization);
        return mail;
    }

}
