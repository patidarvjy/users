package com.docutools.mfa.sms;

/**
 * Simple class to manage SMS sending.
 */
public interface SMSSender {

    /**
     * Send a SMS message with contents provides in {@code message} to phone number informed by {@code phoneNumber}.
     * @param message SMS text body
     * @param phoneNumber The SMS Recipient phone number
     */
    void sendSMSMessage(String message, String phoneNumber);

}
