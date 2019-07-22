package com.docutools.emails;

import com.docutools.subscriptions.SubscriptionType;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PersonalNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PersonalNotificationService.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MailServer mailServer;

    @Value("${docutools.notificationAfter48Hours:false}")
    private boolean enabled;

    public void sendPersonalNotification(UUID userId) {
        if (!enabled) {
            log.info("Not sending personal notification emails, since this Feature disabled!");
            return;
        }

        Optional<DocutoolsUser> optional = userRepo.findById(userId);
        optional.ifPresent(user -> {
            if ((user.getAccount() == null || user.getAccount().getSubscription().getType() == SubscriptionType.Test)
                && user.getOrganisation().getSubscription().getType() == SubscriptionType.Test) {
                //Send Email
                Map<String, Object> properties = new HashMap<>();
                properties.put("user_name", user.getName().toString());
                mailServer.sendEmail(EmailTemplateType.PersonalNotificationAfter48Hours, properties, user);
            }
        });
    }
}
