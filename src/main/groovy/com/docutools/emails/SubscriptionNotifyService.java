package com.docutools.emails;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.UserRepo;
import com.docutools.utils.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionNotifyService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionNotifyService.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MailServer mailServer;

    @Value("${docutools.testPeriodExpiryEmails:false}")
    private boolean enabled;

    public void sendTestSubExpiryNotification() {
        if (!enabled) {
            log.info("Not sending test period expiry emails, since this Feature disabled!");
            return;
        }

        List<DocutoolsUser> usersToNotify3Days = userRepo.findTestUserLicenceExpiringIn(LocalDate.now().plusDays(3));
        List<DocutoolsUser> usersToNotify10Days = userRepo.findTestUserLicenceExpiringIn(LocalDate.now().plusDays(10));
        checkAndSendMail(usersToNotify3Days);
        checkAndSendMail(usersToNotify10Days);
    }

    private void checkAndSendMail(List<DocutoolsUser> users){
        users.stream()
                .filter(user -> Validator.isValidEmail(user.getEmail()))
                .forEach(user -> {
                    Map<String, Object> properties = new HashMap<>();
                    long daysRemaining;
                    if (user.getAccount() != null) {
                        daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), user.getAccount().getActiveUntil());
                    } else {
                        daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), user.getOrganisation().getSubscription().getUntil());
                    }
                    properties.put("user_name", user.getName().toString());
                    mailServer.sendEmail(getEmailType((int) daysRemaining), properties, user);
                });
    }

    private EmailTemplateType getEmailType(int period){
        switch(period){
            case 3:
                return EmailTemplateType.EndTestPhase3Days;
            case 10:
                return EmailTemplateType.EndTestPhase10Days;
            default:
                return null;
        }
    }
}
