package com.docutools.customers;

import au.com.bytecode.opencsv.CSVWriter;
import com.docutools.chargebee.ChargebeeService;
import com.docutools.exceptions.ErrorCodes;
import com.docutools.exceptions.ExceptionHelper;
import com.docutools.oauth2.ClientCredentialsService;
import com.docutools.services.internal.InternalApiClient;
import com.docutools.services.projects.resources.Project;
import com.docutools.subscriptions.Account;
import com.docutools.subscriptions.AccountRepository;
import com.docutools.subscriptions.Subscription;
import com.docutools.subscriptions.SubscriptionType;
import com.docutools.team.MembershipState;
import com.docutools.team.TeamMembership;
import com.docutools.team.TeamMembershipRepo;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserManager;
import com.docutools.users.UserRepo;
import com.docutools.users.resources.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.docutools.exceptions.ExceptionHelper.newResourceNotFoundError;

@Service
@Transactional
public class CustomerService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd. MMM yyyy");

    private static final Logger log = LoggerFactory.getLogger(ChargebeeService.class);

    @Autowired
    private OrganisationRepo organisationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private UserManager userManager;
    @Autowired
    private ClientCredentialsService clientCredentialsService;
    @Autowired
    private TeamMembershipRepo membershipRepo;
    @Autowired
    private InternalApiClient internalApiClient;

    @Value("${docutools.baseUrl}")
    private String baseUrl;
    @Value("${docutools.mail.links.register}")
    private String registerLink;

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomer(UUID id) {
        Assert.notNull(id, "id is required - must not be NULL!");
        Optional<Organisation> organisationOptional = organisationRepository.findById(id);
        if (organisationOptional.isPresent()) {
            Organisation organisation = organisationOptional.get();
            checkAndSetReseller(organisation);
            Customer value = new Customer(organisation);
            checkForClientCredentials(organisation, value);
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    //Should now be done via the chargebee callback
    @Deprecated
    private void checkAndSetReseller(Organisation organisation) {
        if (organisation.getReseller() == null || organisation.getReseller().equals("")) {
            setAndSaveReseller(organisation);
        }
    }

    private void checkForClientCredentials(Organisation organisation, Customer customer) {
        try {
            customer.setHasClientCredentials(clientCredentialsService.hasCredentials(organisation));
        } catch (Exception e) {
            log.error(String.format("Could not check for client credentials on Organisation %s.", organisation.getId()), e);
        }
    }

    //Should now be done via the chargebee callback
    @Deprecated
    private void setAndSaveReseller(Organisation organisation) {
        String reseller = getReseller(organisation.getId());
        organisation.setReseller(reseller);
        organisationRepository.save(organisation);
    }


    //Should now be done via the chargebee callback
    @Deprecated
    private String getReseller(UUID id) {
        try {
            com.chargebee.models.Customer customer = com.chargebee.models.Customer.retrieve(id.toString())
                    .request()
                    .customer();
            return customer.optString("cf_salespartner");
        } catch (Exception e) {
            log.error(String.format("Could not retrieve customer or reseller for id %s", id));
            log.error(e.getMessage());
        }
        return "";
    }

    public Optional<Customer> update(CustomerUpdate update) {
        Assert.notNull(update, "update is required - must not be NULL!");
        Assert.notNull(update.getOrganisationId(), "update.id is required - must not be NULL!");
        return organisationRepository.findById(update.getOrganisationId())
                .map(update::apply)
                .map(organisationRepository::save)
                .map(this::createCustomerAndCheckForClientCredentials);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomersPaged(String search, PageRequest request) {
        Assert.notNull(request, "request is required - must not be NULL!");
        Page<Customer> customers = organisationRepository.findByTerm("%" + search + "%", request);
        customers.forEach(this::checkAndSetInvited);
        return customers;
    }

    private void checkAndSetInvited(Customer customer) {
        //User is counted invited if he has test licence and a membership on a project that don't belongs to his org
        if (customer.getSubscription().getType() != SubscriptionType.Test) {
            return;
        }

        List<TeamMembership> membershipList = membershipRepo.findByUserId(customer.getOwner().getUserId());
        if (membershipList.size() != 1) return;
        TeamMembership teamMembership = membershipList.get(0);

        DocutoolsUser invitedBy = userRepository.getOne(customer.getOwner().getUserId()).getInvitedBy();
        if (invitedBy == null) {
            Project project = internalApiClient.getProjectById(teamMembership.getProjectId()).get();
            boolean isInvited = teamMembership.getState().equals(MembershipState.Active) && project.isActive()
                && !project.getOrganisationId().equals(customer.getOrganisationId());
            if (isInvited) {
                invitedBy = organisationRepository.getOne(project.getOrganisationId()).getOwner();
            } else {
                return;
            }
        }
        customer.setInvited(true);
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(invitedBy.getUsername());
        userDTO.setFirstName(invitedBy.getName().getFirstName());
        userDTO.setLastName(invitedBy.getName().getLastName());
        customer.setInvitedBy(userDTO);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers(String search, Sort sort) {
        Assert.notNull(search, "search is required - must not be NULL!");
        Assert.notNull(sort, "sort is required - must not be NULL!");
        return organisationRepository.findByNameLike("%" + search + "%", sort)
                .map(this::createCustomerAndCheckForClientCredentials)
                .collect(Collectors.toList());
    }

    private Customer createCustomerAndCheckForClientCredentials(Organisation organisation) {
        Customer customer = new Customer(organisation);
        checkForClientCredentials(organisation, customer);
        checkAndSetInvited(customer);
        return customer;
    }

    @Transactional(readOnly = true)
    public void writeAllCustomersAsCsv(OutputStream outputStream, String search, String sortBy, Sort.Direction direction) throws IOException {
        Assert.notNull(outputStream, "outputStream is required - must not be NULL!");

        CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream));
        writer.writeNext("Name", "Country", "Billing Mail", "Owner", "Type", "Payment Plan", "Since", "Until", "Payment Method",
                "Postal Bills", "Accounts Available", "Accounts Used");
        organisationRepository.findByTerm("%" + search + "%", sortBy)
                .map(Customer::new)
                .forEach(customer -> {
                    Subscription subscription = customer.getSubscription();
                    String[] line = new String[]{
                            customer.getName(), customer.getCc(), customer.getBillingMail(),
                            customer.getOwner().getName().toString(),
                            toStringOrEmpty(subscription.getType()),
                            toStringOrEmpty(subscription.getPaymentPlan()),
                            formatOrEmpty(subscription.getSince()),
                            formatOrEmpty(subscription.getUntil()),
                            toStringOrEmpty(subscription.getPaymentType()),
                            yesOrNo(subscription.isPostalBills()),
                            String.valueOf(subscription.countAvailableAccounts()),
                            String.valueOf(subscription.countUsedAccounts())
                    };
                    writer.writeNext(line);
                });
        writer.flush();
    }

    private String toStringOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    private String formatOrEmpty(LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : "";
    }

    private String yesOrNo(boolean yes) {
        return yes ? "Yes" : "No";
    }

    @Transactional(readOnly = true)
    public List<CustomerUser> listEmployees(UUID customerId) {
        Assert.notNull(customerId, "customerId is required - must not be NULL!");
        return organisationRepository.findById(customerId)
                .map(Organisation::getMembers)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(CustomerUser::new)
                .collect(Collectors.toList());
    }

    public CustomerUser inviteUser(UUID customerId, NewCustomerUser user) {
        Assert.notNull(customerId, "customerId is required - must not be NULL!");
        Assert.notNull(user, "user is required - must not be NULL!");
        Organisation organisation = organisationRepository.findById(customerId)
                .orElseThrow(() -> newResourceNotFoundError(String.format("Customer %s", customerId)));
        String email = user.getEmail();
        DocutoolsUser docutoolsUser = userManager.loadOrCreateUser(email, organisation.getId());
        if (!docutoolsUser.isMemberOf(organisation)) {
            docutoolsUser.setOrganisation(organisation);
            userRepository.save(docutoolsUser);
            Account account = docutoolsUser.getAccount();
            if (account != null) {
                account.removeAssignment();
                accountRepository.save(account);
            }
        }
        return new CustomerUser(docutoolsUser);
    }

    @Transactional(readOnly = true)
    public List<CustomerAccount> getAllCustomerAccounts(UUID customerId) {
        Assert.notNull(customerId, "customerId is required - must not be NULL!");
        String registrationBase = String.format("%s%s", baseUrl, registerLink);
        return organisationRepository.findById(customerId)
                .map(Organisation::getSubscription)
                .map(Subscription::getAccounts)
                .map(accounts -> accounts.stream()
                        .map(CustomerAccount::new)
                        .peek(customerAccount -> customerAccount.generateActivationLink(registrationBase))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @Transactional(readOnly = true)
    public List<AccountHolder> getAllUnassignedUsers(UUID customerId) {
        Assert.notNull(customerId, "customerId is required - must not be NULL!");
        String registrationBase = String.format("%s%s", baseUrl, registerLink);
        return organisationRepository.findById(customerId)
                .map(Organisation::getMembers)
                .map(members -> members.stream()
                        .filter(m -> m.getAccount() == null)
                        .map(AccountHolder::new)
                        .peek(accountHolder -> accountHolder.generateActivationLink(registrationBase))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @Transactional(readOnly = true)
    public Page<AccountHolder> getUsersPaged(UUID customerId, PageRequest request) {
        Assert.notNull(customerId, "customerId is required - must not be NULL!");
        String registrationBase = String.format("%s%s", baseUrl, registerLink);
        return organisationRepository.findMembersByIdPaged(customerId, request)
                .map(AccountHolder::new)
                .map(accountHolder -> {
                    accountHolder.generateActivationLink(registrationBase);
                    return accountHolder;
                });
    }

    public Optional<CustomerAccount> addAccount(UUID customerId) {
        Assert.notNull(customerId, "customerId is required - must not be NULL!");
        return organisationRepository.findById(customerId)
                .map(Organisation::getSubscription)
                .map(Account::new)
                .map(accountRepository::save)
                .map(CustomerAccount::new);
    }

    public Optional<CustomerAccount> deleteAccount(UUID accountId) {
        Assert.notNull(accountId, "accountId is required - must not be NULL!");
        return accountRepository.findById(accountId)
                .map(account -> {
                    accountRepository.delete(account);
                    return new CustomerAccount(account);
                });
    }

    public Optional<CustomerAccount> assignAccount(UUID accountId, AccountHolder holder) {
        Assert.notNull(accountId, "accountId is required - must not be NULL!");
        Assert.notNull(holder, "holder is required - must not be NULL!");
        Assert.notNull(holder.getUserId(), "holder.userId is required - must not be NULL!");
        DocutoolsUser newAssignee = userRepository.findById(holder.getUserId())
                .orElseThrow(() -> ExceptionHelper.newBadRequestError(ErrorCodes.USER_NOT_FOUND));

        if (newAssignee.hasActiveAccount()) {
            Account account = newAssignee.getAccount();
            accountRepository.save(account);
            return Optional.of(account)
                    .map(CustomerAccount::new);
        }
        return accountRepository.findById(accountId)
                .map(account -> account.assign(newAssignee))
                .map(accountRepository::save)
                .map(CustomerAccount::new);
    }

    public Optional<CustomerAccount> unassignAccount(UUID accountId) {
        Assert.notNull(accountId, "accountId is required - must not be NULL!");
        return accountRepository.findById(accountId)
                .map(Account::removeAssignment)
                .map(accountRepository::save)
                .map(CustomerAccount::new);
    }
}
