package com.docutools.subscriptions;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class SubscriptionsCrud {

    @Autowired
    private SubscriptionRepository subsRepository;

    @Autowired
    private OrganisationRepo organisationRepository;
    @Autowired
    private UserRepo userRepository;

    private Organisation testOrganisation;
    private DocutoolsUser testUser;

    @BeforeEach
    public void setup() {
        this.testOrganisation = organisationRepository.save(new Organisation());
        this.testUser = userRepository.save(new DocutoolsUser("freddy.krueger@example.com", testOrganisation));
    }

    @Test
    @DisplayName("Create Subscription via Organisation.")
    public void createSubscriptionViaOrganisation() {
        Subscription subscription = testOrganisation.getSubscription();
        assertThat(subscription, notNullValue());
        assertThat(subsRepository.existsById(subscription.getId()), is(true));
    }

    @Test
    @DisplayName("Update Subscriptions.")
    public void updateSubscriptions() {
        // Arrange
        Subscription subscription = testOrganisation.getSubscription();

        // Act
        Account account = subscription.newAccount().orElseThrow(AssertionError::new);
        account.assign(this.testUser);
        subsRepository.save(subscription);

        // Assert
        Subscription actual = subsRepository.getOne(subscription.getId());

        assertThat(actual, notNullValue());
        assertThat(actual.getFreeAccount().isPresent(), is(false));
        assertThat(actual.getAccounts(), hasSize(1));
        Account actualAccount = actual.getAccounts().stream().findFirst().orElse(null);
        assertThat(actualAccount, notNullValue());
        assertThat(actualAccount.isUnassigned(), is(false));
    }

}
