package com.docutools.chargebee;

public class NoChargebeeConfigException extends RuntimeException {

    public NoChargebeeConfigException() {
    }

    public NoChargebeeConfigException(String message) {
        super(message);
    }

    public NoChargebeeConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoChargebeeConfigException(Throwable cause) {
        super(cause);
    }

    public NoChargebeeConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
