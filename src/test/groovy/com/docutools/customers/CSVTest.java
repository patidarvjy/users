package com.docutools.customers;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class CSVTest {

    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CustomerService customerService;


    @Test
    public void shouldCreateCSVWithAllMatchingOrgansations() throws IOException {
        // Arrange
        String searchTerm = "Find";

        Organisation organisationMatchName = new Organisation();
        organisationMatchName.setId(UUID.fromString("8c762962-6d9e-4d7c-b42d-91ad2668b674"));
        organisationMatchName.setName("Find #1");
        organisationMatchName.setBillingMail("nothing@gmail.com");
        organisationMatchName.setCc("AT");

        Organisation organisationMatchBillingMail = new Organisation();
        organisationMatchBillingMail.setName("Nada");
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

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String expectedString = "\"Name\",\"Country\",\"Billing Mail\",\"Owner\",\"Type\",\"Payment Plan\",\"Since\",\"Until\",\"Payment Method\",\"Postal Bills\",\"Accounts Available\",\"Accounts Used\"\n" +
                "\"Find #1\",\"at\",\"nothing@gmail.com\",\" \",\"Test\",\"\",\"01. abr 2019\",\"01. may 2019\",\"\",\"No\",\"0\",\"0\"\n" +
                "\"Nada\",\"at\",\"find@gmail.com\",\" \",\"Test\",\"\",\"01. abr 2019\",\"01. may 2019\",\"\",\"No\",\"0\",\"0\"\n";

        // Act
        customerService.writeAllCustomersAsCsv(byteArrayOutputStream, searchTerm, "name", Sort.Direction.ASC);
        String result = new String(byteArrayOutputStream.toByteArray());

        // Assert
        assertEquals(expectedString, result);
    }
}
