package com.docutools.emails.mailservers;

import com.docutools.emails.EmailTemplateType;
import com.docutools.emails.MailServer;
import com.docutools.emails.SendEmailException;
import com.docutools.emails.tempaltes.CustomEmailTemplate;
import com.docutools.emails.tempaltes.CustomEmailTemplateRepository;
import com.docutools.emails.tempaltes.EmailTemplates;
import com.docutools.users.DocutoolsUser;
import com.sendgrid.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class SendGridMailServer implements MailServer {

    private static final Logger log = LoggerFactory.getLogger(MailServer.class);

    @Autowired
    private EmailTemplates templates;
    private boolean enabled;
    private final Email fromEmail;

    private SendGrid sendGrid;

    @Value("${docutools.baseUrl}")
    private String url;

    @Autowired
    private CustomEmailTemplateRepository customEmailTemplateRepository;


    public SendGridMailServer(@Value("${docutools.mail.enabled:false}") boolean enabled,
                              @Value("${docutools.sendGrid.apiKey:}") String sendGridApiKey,
                              @Value("${docutools.mail.from}") String from) {
        this.enabled = enabled;
        this.fromEmail = new Email(from);
        if(enabled && StringUtils.isEmpty(sendGridApiKey)) {
            throw new BeanCreationException("When MailServer is enabled you have to provided the docutools.sendGrid.apiKey property!");
        }
        if(enabled) {
            this.sendGrid = new SendGrid(sendGridApiKey);
        }
        log.debug("Initialized SendGridMailServer");
    }

    @Override
    public CompletableFuture sendEmail(EmailTemplateType templateType, Map<String, Object> properties, DocutoolsUser user) {
        Assert.notNull(templateType, "templateType is required - must not be NULL!");
        Assert.notNull(properties, "properties is required - must not be NULL!");
        Assert.notNull(user, "user is required - must not be NULL!");

        return CompletableFuture.runAsync(() -> doSendEmail(templateType, properties, user));
    }

    private void doSendEmail(EmailTemplateType templateType, Map<String, Object> properties, DocutoolsUser user) {
        Optional<CustomEmailTemplate> optEmailTemplate = customEmailTemplateRepository.findByOrganisationIdAndLanguageIsAndNotificationTypeIsAndDefaultTemplateIsFalse(user.getOrganisation().getId(), user.getSettings().getLanguage(), templateType);
        if(!optEmailTemplate.isPresent()){
            optEmailTemplate = customEmailTemplateRepository.findByNotificationTypeAndDefaultTemplateIsTrueAndLanguageIs(templateType, user.getSettings().getLanguage());
        }
        if(!optEmailTemplate.isPresent()){
            optEmailTemplate = customEmailTemplateRepository.findByNotificationTypeAndDefaultTemplateIsTrueAndLanguageIs(templateType, "en");
        }
        if(!optEmailTemplate.isPresent()){
            throw new SendEmailException("Could not find email template!");
        }
        CustomEmailTemplate customEmailTemplate = optEmailTemplate.get();
        Mail customMail = new Mail();
        customMail.setFrom(fromEmail);
        customMail.addCategory(getMailCategory(templateType));
        customMail.setTemplateId(customEmailTemplate.getSendGridTemplateId().toString());
        String userEmail = user.getEmail();
        if(StringUtils.isEmpty(userEmail) || !userEmail.contains("@")) {
            userEmail = user.getUsername();
        }
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(userEmail, user.getName().toString()));
        if (properties != null) {
            properties.forEach((key, value) -> personalization.addSubstitution(key, value.toString()));
        }
        personalization.addSubstitution("-url-", url);
        personalization.addSubstitution("user_name", user.getName().toString());
        personalization.addSubstitution("-token-", user.getVerificationStatus().getToken());
        personalization.addSubstitution("-usersLanguageCode-", user.getSettings().getLanguage());
        customMail.addPersonalization(personalization);

        if(!enabled) {
            log.info("Not sending {} Mail to {}, since Mail Feature disabled!", templateType, user.getUsername());
            return;
        }
        if(user.getUsername().endsWith("@example.com")) {
            log.info("Not sending to @example.com");
            return;
        }

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(customMail.build());


            Response response = sendGrid.api(request);
            if(response.getStatusCode() == 202) {
                log.debug("Successfully sent {} Email to {}!", templateType, user.getUsername());
            } else {
                log.error("Failure when sending {} Email to {}: Status Code={}, Message={}",
                        templateType, user.getUsername(), response.getStatusCode(), response.getBody());
                throw new SendEmailException();
            }
        } catch (IOException e) {
            log.error(String.format("Fialure when sending %s Email to %s: %s", templateType, user.getUsername(), e.getMessage()), e);
            throw new SendEmailException(e);
        }
    }

    private String getMailCategory(EmailTemplateType templateType) {
        switch(templateType){
            case Register:
                return "register";
            case Invitation:
                return "invitation";
            case ChangeEmail:
                return "email-change";
            case EndTestPhase: //Deprecated
            case EndTestPhase3Days:
            case EndTestPhase10Days:
                return "end-test-phase";
            case TokenExpired:
                return "token-expired";
            case ForgotPassword:
                return "password-recovery";
            case InviteToProject:
                return "project-invitation";
            case PersonalNotificationAfter48Hours:
                return "personal-notification-after-48-hours";
            default:
                return null;
        }
    }

}
