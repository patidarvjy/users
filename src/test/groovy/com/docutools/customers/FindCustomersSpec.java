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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class FindCustomersSpec {

    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomersController customersController;

    @Test
    public void shouldFindOrganisationsByNameOrBillingMailCaseInsensitive() {
        // Arrange
        String searchTerm = "Find";

        Organisation organisationMatchName = new Organisation();
        organisationMatchName.setId(UUID.randomUUID());
        organisationMatchName.setName("Find #1");
        organisationMatchName.setCc("AT");

        Organisation organisationMatchBillingMail = new Organisation();
        organisationMatchBillingMail.setBillingMail("find@gmail.com");
        organisationMatchBillingMail.setCc("AT");

        Organisation organisationNoMatch = new Organisation();
        organisationNoMatch.setCc("DE");

        Arrays.asList(organisationMatchName, organisationMatchBillingMail, organisationNoMatch)
                .forEach(organisation -> {
                    organisationRepo.save(organisation);
                    DocutoolsUser owner = new DocutoolsUser(UUID.randomUUID().toString() + "@gmail.com", organisation);
                    userRepo.save(owner);
                    organisation.setOwner(owner);
                    organisationRepo.save(organisation);
                });

        Page<Customer> result = organisationRepo.findByTerm("%" + searchTerm + "%", PageRequest.of(0, 5));

        // Assert
        assertTrue(result.getContent().stream().anyMatch(customer -> customer.getName().equals(organisationMatchName.getName())));
        assertTrue(result.getContent().stream().anyMatch(customer -> customer.getBillingMail().equals(organisationMatchBillingMail.getBillingMail())));
        assertEquals(2L, result.getTotalElements());
    }

    @Test
    public void findMailNoBillingMailSet() {
        // Arrange
        String searchTerm = "test@test.com";

        Organisation organisation = new Organisation();

        organisationRepo.save(organisation);

        DocutoolsUser owner = new DocutoolsUser("user", organisation);
        owner.setEmail("test@test.com");

        userRepo.save(owner);
        organisation.setOwner(owner);
        organisationRepo.save(organisation);

        //Act
        Page<Customer> result = organisationRepo.findByTerm("%" + searchTerm + "%", PageRequest.of(0, 5));

        //Assert
        assertTrue(result.getContent().stream().anyMatch(customer -> customer.getOrganisationId().equals(organisation.getId())));
    }

}
