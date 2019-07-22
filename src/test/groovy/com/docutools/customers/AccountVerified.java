package com.docutools.customers;

import com.docutools.subscriptions.Account;
import com.docutools.subscriptions.AccountRepository;
import com.docutools.subscriptions.Subscription;
import com.docutools.subscriptions.SubscriptionRepository;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class AccountVerified {

    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SubscriptionRepository subscriptionRepo;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private CustomerService customerService;


    @SuppressWarnings("ConstantConditions")
    @Test
    public void verifiedGetsSetCorrectly() {
        // Arrange
        Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userRepo.save(userVerified);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userRepo.save(userNotVerified);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account accountVerified = subscription.newAccount().get();
        accountVerified.assign(userVerified);
        accountRepo.save(accountVerified);

        subscriptionRepo.save(accountVerified.getSubscription());

        Account accountNotVerified = subscription.newAccount().get();
        accountNotVerified.assign(userNotVerified);
        accountRepo.save(accountNotVerified);

        subscriptionRepo.save(accountNotVerified.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = organisation.getSubscription().getAccounts().stream()
                .map(CustomerAccount::new)
                .collect(Collectors.toList());

        // Assert
        assertTrue(customerAccounts.get(0).getHolder().isVerified() || customerAccounts.get(1).getHolder().isVerified());
        assertTrue(!customerAccounts.get(0).getHolder().isVerified() || !customerAccounts.get(1).getHolder().isVerified());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void verifiedIsNull() {
        // Arrange
        Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisationRepo.save(organisation);

        DocutoolsUser user = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        user.setVerificationStatus(null);
        userRepo.save(user);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account account = subscription.newAccount().get();
        account.assign(user);
        accountRepo.save(account);

        subscriptionRepo.save(account.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = organisation.getSubscription().getAccounts().stream()
                .map(CustomerAccount::new)
                .collect(Collectors.toList());

        // Assert
        assertTrue(!customerAccounts.get(0).getHolder().isVerified());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void verificationLink() {
        // Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("TestOrg");
        organisationRepo.save(organisation);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userNotVerified.getVerificationStatus().setToken("testToken");
        userRepo.save(userNotVerified);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account accountNotVerified = subscription.newAccount().get();
        accountNotVerified.assign(userNotVerified);
        accountRepo.save(accountNotVerified);

        subscriptionRepo.save(accountNotVerified.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = customerService.getAllCustomerAccounts(organisationId);

        // Assert
        assertEquals("https://staging.docu.solutions/activate/registered/testToken",
                customerAccounts.get(0).getHolder().getActivationLink());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void notVerfiedLinkTokenNull() {
        // Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("TestOrg");
        organisationRepo.save(organisation);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userNotVerified.getVerificationStatus().setToken(null);
        userRepo.save(userNotVerified);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account accountNotVerified = subscription.newAccount().get();
        accountNotVerified.assign(userNotVerified);
        accountRepo.save(accountNotVerified);

        subscriptionRepo.save(accountNotVerified.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = customerService.getAllCustomerAccounts(organisationId);

        // Assert
        assertEquals("https://staging.docu.solutions/activate/registered/null",
                customerAccounts.get(0).getHolder().getActivationLink());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void verfiedLinkTokenNull() {
        // Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("TestOrg");
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userVerified.getVerificationStatus().setToken(null);
        userRepo.save(userVerified);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account accountVerified = subscription.newAccount().get();
        accountVerified.assign(userVerified);
        accountRepo.save(accountVerified);

        subscriptionRepo.save(accountVerified.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = customerService.getAllCustomerAccounts(organisationId);

        // Assert
        assertEquals("",
                customerAccounts.get(0).getHolder().getActivationLink());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void verfiedLinkTokenSet() {
        // Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("TestOrg");
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userVerified.getVerificationStatus().setToken("notVisible");
        userRepo.save(userVerified);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account accountVerified = subscription.newAccount().get();
        accountVerified.assign(userVerified);
        accountRepo.save(accountVerified);

        subscriptionRepo.save(accountVerified.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = customerService.getAllCustomerAccounts(organisationId);

        // Assert
        assertEquals("",
                customerAccounts.get(0).getHolder().getActivationLink());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void notVerifiedHolderNull() {
        // Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("TestOrg");
        organisationRepo.save(organisation);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userRepo.save(userNotVerified);

        Subscription subscription = new Subscription(organisation);
        subscriptionRepo.save(subscription);

        organisation.setSubscription(subscription);
        organisationRepo.save(organisation);

        Account accountVerified = subscription.newAccount().get();
        accountVerified.removeAssignment();
        accountRepo.save(accountVerified);

        subscriptionRepo.save(accountVerified.getSubscription());

        // Act
        List<CustomerAccount> customerAccounts = customerService.getAllCustomerAccounts(organisationId);

        // Assert
        assertEquals("",
                customerAccounts.get(0).getActivationLink());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void unassignedActivationLink() {
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("TestOrg");
        organisationRepo.save(organisation);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userNotVerified.getVerificationStatus().setToken("testToken");
        userNotVerified.setAccount(null);
        userRepo.save(userNotVerified);

        organisation.setMembers(Collections.singletonList(userNotVerified));
        organisationRepo.save(organisation);

        // Act
        List<AccountHolder> customerAccounts = customerService.getAllUnassignedUsers(organisationId);

        // Assert
        assertEquals("https://staging.docu.solutions/activate/registered/testToken",
                customerAccounts.get(0).getActivationLink());
    }

}