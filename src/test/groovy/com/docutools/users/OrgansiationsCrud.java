package com.docutools.users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class OrgansiationsCrud {

    @Autowired
    private OrganisationRepo organisationRepo;

    @Test
    @DisplayName("Create Organisation")
    public void createOrgnaisaiton() {
        // Arrange
        Organisation organisation = new Organisation();
        Map<String, String> noLicenseMessages = new HashMap<>();
        noLicenseMessages.put("de", "Hallo Welt!");
        noLicenseMessages.put("en", "Hello World!");
        // Arrange
        organisation.setNoLicenseMessages(noLicenseMessages);
        Organisation actual = organisationRepo.saveAndFlush(organisation);
        // Assert
        assertThat(actual.getNoLicenseMessages().size(), is(2));
    }

}
