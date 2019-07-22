package com.docutools.customers;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class EmployeesTest {

    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CustomerService customerService;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getEmployeesPaged() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userRepo.save(userVerified);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userRepo.save(userNotVerified);

        ArrayList<DocutoolsUser> docutoolsUsers = new ArrayList<>();
        docutoolsUsers.add(userNotVerified);
        docutoolsUsers.add(userVerified);
        organisation.setMembers(docutoolsUsers);
        organisationRepo.save(organisation);

        //Act
        Page<AccountHolder> usersPaged = customerService.getUsersPaged(organisationId, PageRequest.of(0, 10));

        //Verify
        assertEquals(2, usersPaged.getNumberOfElements());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void pageCorrectSize() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userRepo.save(userVerified);

        DocutoolsUser userNotVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userRepo.save(userNotVerified);

        ArrayList<DocutoolsUser> docutoolsUsers = new ArrayList<>();
        docutoolsUsers.add(userNotVerified);
        docutoolsUsers.add(userVerified);
        organisation.setMembers(docutoolsUsers);
        organisationRepo.save(organisation);

        //Act
        Page<DocutoolsUser> usersPaged = organisationRepo.findMembersByIdPaged(organisationId, PageRequest.of(0, 1));
//        Page<DocutoolsUser> usersPaged = userRepo.findByOrganisationId(organisationId, "", PageRequest.of(1, 1));

        //Verify
        assertEquals(1, usersPaged.getNumberOfElements());
    }

    @Test
    public void billingEmailIsOrgEmailIfAvailable() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setBillingMail("orgemail@hello.com");
        organisationRepo.save(organisation);

        DocutoolsUser owner = new DocutoolsUser("username", organisation);
        owner.setEmail("useremail@hello.com");
        userRepo.save(owner);

        organisation.setOwner(owner);
        organisationRepo.save(organisation);

        //Act
        PageRequest pageRequest = PageRequest.of(0, 25, Sort.Direction.ASC, "name");
        Page<Customer> customersPaged = customerService.getCustomersPaged("", pageRequest);

        //Verify
        assertEquals("orgemail@hello.com", customersPaged.getContent().get(0).getBillingMail());
    }

    @Test
    public void billingEmailIsOwnerEmailIfAvailableAndNoOrgEmail() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisationRepo.save(organisation);

        DocutoolsUser owner = new DocutoolsUser("username", organisation);
        owner.setEmail("useremail@hello.com");
        userRepo.save(owner);

        organisation.setOwner(owner);
        organisationRepo.save(organisation);

        //Act
        PageRequest pageRequest = PageRequest.of(0, 25, Sort.Direction.ASC, "name");
        Page<Customer> customersPaged = customerService.getCustomersPaged("", pageRequest);

        //Verify
        assertEquals("useremail@hello.com", customersPaged.getContent().get(0).getBillingMail());
    }

    @Test
    public void billingEmailIsOwnerUsernameIfAvailableAndNoOrgEmailAndNoOwnerEmail() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisationRepo.save(organisation);

        DocutoolsUser owner = new DocutoolsUser("username", organisation);
        userRepo.save(owner);

        organisation.setOwner(owner);
        organisationRepo.save(organisation);

        //Act
        PageRequest pageRequest = PageRequest.of(0, 25, Sort.Direction.ASC, "name");
        Page<Customer> customersPaged = customerService.getCustomersPaged("", pageRequest);

        //Verify
        assertEquals("username", customersPaged.getContent().get(0).getBillingMail());
    }

    @SuppressWarnings("ConstantConditions")
//    @Test
    public void pageSortingAsc() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser("A", organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userRepo.save(userVerified);

        DocutoolsUser userNotVerified = new DocutoolsUser("B", organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userRepo.save(userNotVerified);

        ArrayList<DocutoolsUser> docutoolsUsers = new ArrayList<>();
        docutoolsUsers.add(userNotVerified);
        docutoolsUsers.add(userVerified);
        organisation.setMembers(docutoolsUsers);
        organisationRepo.save(organisation);

        //Act
        Page<AccountHolder> usersPaged = customerService.getUsersPaged(organisationId, PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "m.email")));

        //Verify
        assertEquals("a", usersPaged.getContent().get(0).getEmail());
    }

    @SuppressWarnings("ConstantConditions")
//    @Test

    public void pageSortingDes() {
        //Arrange
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser("A", organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userRepo.save(userVerified);

        DocutoolsUser userNotVerified = new DocutoolsUser("B", organisation);
        userNotVerified.getVerificationStatus().setVerified(false);
        userRepo.save(userNotVerified);

        ArrayList<DocutoolsUser> docutoolsUsers = new ArrayList<>();
        docutoolsUsers.add(userNotVerified);
        docutoolsUsers.add(userVerified);
        organisation.setMembers(docutoolsUsers);
        organisationRepo.save(organisation);

        //Act
        Page<AccountHolder> usersPaged = customerService.getUsersPaged(organisationId, PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "m.email")));

        //Verify
        assertEquals("b", usersPaged.getContent().get(0).getEmail());
    }
}
