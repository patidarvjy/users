package com.docutools.emails.tempaltes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Could not load email template.")
public class EmailTemplateException extends RuntimeException {
    public EmailTemplateException() {
    }

    public EmailTemplateException(String message) {
        super(message);
    }

    public EmailTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailTemplateException(Throwable cause) {
        super(cause);
    }

    public EmailTemplateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
