package com.docutools.subscriptions;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.SessionManager;
import com.docutools.users.UserRepo;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserRepo userRepo;

    @ApiOperation(value = "Get Subscription of the Current User")
    @GetMapping(path = "/api/v2/me/subscription")
    public Subscription getSubscription() {
        log.debug("GET /api/v2/subscription");
        DocutoolsUser user = sessionManager.getCurrentUser();
        Organisation organisation = user.getOrganisation();
        Subscription subscription = organisation.getSubscription();
        if(subscription == null) {
            subscription = new Subscription(organisation);
            subscriptionRepository.save(subscription);
            subscription.newAccount().ifPresent(acc -> {
                acc.assign(user);
                subscriptionRepository.save(acc.getSubscription());
            });
        }
        return subscription;
    }

}
