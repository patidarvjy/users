package com.docutools.emails.tempaltes;

import com.docutools.emails.EmailTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomEmailTemplateRepository extends JpaRepository<CustomEmailTemplate, UUID> {
    Optional<CustomEmailTemplate> findByOrganisationIdAndLanguageIsAndNotificationTypeIsAndDefaultTemplateIsFalse(UUID orgId, String language, EmailTemplateType type);
    Optional<CustomEmailTemplate> findByNotificationTypeAndDefaultTemplateIsTrueAndLanguageIs(EmailTemplateType type, String language);
}
