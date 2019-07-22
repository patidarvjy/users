package com.docutools.mfa.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TwilioSMSSender implements SMSSender {

    private static final Logger log = LoggerFactory.getLogger(SMSSender.class);

    public TwilioSMSSender(@Value("${docutools.security.mfa.twilio.accountSid:}") String sid,
                           @Value("${docutools.security.mfa.twilio.authToken:}") String token) {
        log.info("Twilio Credentials: {}, {}", sid, token);
        Twilio.init(sid, token);
    }

    @Override
    public void sendSMSMessage(String message, String phoneNumber) {
        Message sms = Message
                .creator(new PhoneNumber(phoneNumber), // to
                        new PhoneNumber("+43676800104070"), // from
                        message)
                .create();
        log.debug("Sent SMS Message over Twilio: {}", sms.getSid());
    }

}
