package com.docutools.mfa.sms

import org.junit.Test

class SMSSenderTest {

    @Test
    public void sendSMSMessageTest() {
        String phoneNumber = "+436642570425"
        String message = "It's a sample"

        SMSSender sender = new TwilioSMSSender("ACbc612b9c6cceb8b9a05bfd645016a03a", "****");

        sender.sendSMSMessage(message, phoneNumber)
    }

}
