package com.docutools.customers;


import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class CustomersSort {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrganisationRepo organisationRepo;

    @Autowired
    private UserRepo userRepo;


    @BeforeEach
    public void setup() {
        Organisation organisation = new Organisation();
        UUID organisationId = UUID.randomUUID();
        organisation.setId(organisationId);
        organisation.setName("hello");
        organisationRepo.save(organisation);

        DocutoolsUser userVerified = new DocutoolsUser(UUID.randomUUID().toString(), organisation);
        userVerified.getVerificationStatus().setVerified(true);
        userRepo.save(userVerified);


        organisation.setOwner(userVerified);
        organisationRepo.save(organisation);


    }


    @ParameterizedTest
    @ValueSource(strings = {"name", "created", "owner.name.lastName", "subscription.type",
            "subscription.paymentPlan", "subscription.since", "subscription.until"})
    public void testSort(String sortBy) {
        PageRequest pageRequest = PageRequest.of(0, 25, Sort.Direction.ASC, sortBy);
        Page<Customer> customersPaged = customerService.getCustomersPaged("", pageRequest);
        assertEquals(1L, customersPaged.getTotalElements());
    }
}
