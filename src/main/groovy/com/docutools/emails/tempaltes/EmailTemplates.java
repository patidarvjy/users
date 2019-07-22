package com.docutools.emails.tempaltes;

import com.docutools.users.DocutoolsUser;
import com.docutools.emails.EmailTemplateType;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class EmailTemplates {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplates.class);

    @Value("${docutools.baseUrl}")
    private String url;
    @Value("${docutools.mail.links.register}")
    private String registerLink;
    @Value("${docutools.mail.links.invite}")
    private String invitationLink;
    @Value("${docutools.mail.links.forgotPassword}")
    private String forgotPasswordLink;
    @Value("${docutools.mail.links.changeEmail}")
    private String changeEmailLink;
    @Value("${docutools.mail.links.help}")
    private String helpLink;

    private Configuration freemarker;
    private ResourceBundleMessageSource subjectLineSource;

    @Autowired
    public EmailTemplates(Configuration freemarker) {
        this.freemarker = freemarker;
        freemarker.setClassForTemplateLoading(getClass(), "/i18/email/html");
        subjectLineSource = new ResourceBundleMessageSource();
        subjectLineSource.setBasename("i18/email/SubjectLines");
    }

    public EmailTemplate loadTemplate(EmailTemplateType templateType, Map<String, Object> properties, DocutoolsUser user) {
        Assert.notNull(templateType, "templateType is required - must not be NULL!");
        Assert.notNull(properties, "properties is required - must not be NULL!");
        Assert.notNull(user, "user is required - must not be NULL!");

        try {
            String freemarkerName = String.format("%s.ftl", templateType);
            Locale userLocale = Locale.forLanguageTag(user.getSettings().getLanguage());
            Template template = freemarker.getTemplate(freemarkerName, userLocale);
            properties.put("links", getLinkMap());
            properties.put("user", getUserMap(user));
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, properties);
            String subjectLine = subjectLineSource.getMessage(templateType.toString(), null, userLocale);
            return new EmailTemplate(subjectLine, html);
        } catch (IOException | TemplateException e) {
            log.error(String.format("Error Loading Email Template (type=%s, properties= %s, user= %s)",
                    templateType, properties, user.getUsername()), e);
            throw new EmailTemplateException(e);
        }
    }

    private Map<String, Object> getLinkMap() {
        Map<String, Object> links = new HashMap<>();
        links.put("url", url);
        links.put("register", url + registerLink);
        links.put("invite", url + invitationLink);
        links.put("forgotPassword", url + forgotPasswordLink);
        links.put("changeEmail", url + changeEmailLink);
        links.put("help", helpLink);
        return links;
    }

    private Map<String, Object> getUserMap(DocutoolsUser user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getName().toString());
        userMap.put("email", user.getUsername());
        userMap.put("verificationToken", user.getVerificationStatus().getToken());
        userMap.put("organisation", user.getOrganisation().getName());
        return userMap;
    }

}
