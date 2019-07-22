package com.docutools.mfa.sms;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AwsSnsSmsSender implements SMSSender {
    private static final Logger log = LoggerFactory.getLogger(SMSSender.class);

    public void sendSMSMessage(String message, String phoneNumber) {
        log.debug("Send SMS message");


        Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
        smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue()
                .withStringValue("docutools") //The sender ID shown on the device.
                .withDataType("String"));
        smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                .withStringValue("Transactional") //Sets the type to promotional.
                .withDataType("String"));

        AmazonSNS sns = AmazonSNSClientBuilder.standard()
                .withRegion(Regions.EU_WEST_1)
                .build();

        PublishResult result = sns.publish(new PublishRequest()
                .withPhoneNumber(phoneNumber)
                .withMessageAttributes(smsAttributes)
                .withMessage(message));

        String messageId = result.getMessageId();

        log.debug("SMS message send <{}>", messageId);
    }
}
