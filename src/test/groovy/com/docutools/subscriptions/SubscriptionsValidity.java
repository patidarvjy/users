package com.docutools.subscriptions;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.resources.UserDTO;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class SubscriptionsValidity {

    @Test
    public void licenseIsActiveUntilEndOfLastDay() {
        //GIVEN
        Organisation organisation = new Organisation();
        Subscription subscription = new Subscription(SubscriptionType.Combo, PaymentPlan.Annually, PaymentType.CreditCard, false, LocalDate.now());
        organisation.setSubscription(subscription);
        DocutoolsUser docutoolsUser = new DocutoolsUser("Test", organisation);

        //WHEN
        UserDTO userDTO = new UserDTO(docutoolsUser);

        //THEN
        assertThat(userDTO.getLicense().getUntil()).isAfter(Instant.now());
    }
}
