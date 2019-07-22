package com.docutools.emails.tempaltes;


import com.docutools.emails.EmailTemplateType;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "email_templates")
public class CustomEmailTemplate {
    @Id
    @Type(type = "pg-uuid")
    private UUID sendGridTemplateId;
    @Column(nullable = false)
    private UUID organisationId;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailTemplateType notificationType;
    @Column(nullable = false)
    private boolean defaultTemplate;
    @Column(nullable = false)
    private String language;

    public UUID getSendGridTemplateId() {
        return sendGridTemplateId;
    }

    public void setSendGridTemplateId(UUID sendGridTemplateId) {
        this.sendGridTemplateId = sendGridTemplateId;
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(UUID organisationId) {
        this.organisationId = organisationId;
    }

    public EmailTemplateType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(EmailTemplateType notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(boolean defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
