package com.docutools.emails;

import com.docutools.users.DocutoolsUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for Service sending Emails to docu tools users.
 *
 * @author amp
 * @since 1.0.0
 */
public interface MailServer {

    CompletableFuture sendEmail(EmailTemplateType template, Map<String, Object> properties, DocutoolsUser recipient);

    default CompletableFuture sendEmail(EmailTemplateType type, DocutoolsUser recipient) {
        return sendEmail(type, new HashMap<>(), recipient);
    }

}
