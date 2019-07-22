package com.docutools.users;

import com.docutools.test.AlternativeFacts;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class OrganisationNameCrud {

    @Autowired
    private OrganisationRepo organisationRepository;
    @Autowired
    private UserRepo userRepository;

    @Test
    @DisplayName("Define a Organisation Name.")
    public void defineOrganisationName() {
        // Arrange
        Organisation organisation = testOrg();
        OrganisationName newName = new OrganisationName("Crysler Corps", organisation);
        // Act
        organisation.getNames().add(newName);
        organisationRepository.save(organisation);
        // Assert
        Organisation actualOrganisation = getOne(organisation);
        List<OrganisationName> actualNames = actualOrganisation.getNames();
        assertThat(actualNames, hasSize(1));
        OrganisationName actualName = actualNames.stream().findFirst().orElse(null);
        assertThat(actualName, equalTo(newName));
    }

    @Test
    @DisplayName("Add and Remove multiple Projects at once.")
    public void addAndRemoveMultipleProjectsAtOnce() {
        // Arrange
        Organisation organisation = testOrg();
        OrganisationName mafiosi = new OrganisationName("Pizza Mafiosi", organisation);
        OrganisationName casaMia = new OrganisationName("Pizza Casa Mia", organisation);
        organisation.getNames().add(mafiosi);
        organisation.getNames().add(casaMia);
        organisationRepository.save(organisation);
        // Act
        Organisation theOrganisation = getOne(organisation);
        OrganisationName mamaMia = new OrganisationName("Pizza Mama Mia", organisation);
        theOrganisation.getNames().add(mamaMia);
        theOrganisation.getNames().remove(casaMia);
        organisationRepository.save(theOrganisation);
        // Assert
        Organisation actualOrganisation = getOne(organisation);
        List<OrganisationName> actualNames = actualOrganisation.getNames();
        assertThat(actualNames, hasSize(2));
        assertThat(actualNames, containsInAnyOrder(mafiosi, mamaMia));
    }

    @Test
    @DisplayName("Don't allow same Company Name in Organisation Twice.")
    public void doNotAllowSameCompanyNameTwice() {
        // Arrange
        Organisation organisation = testOrg();
        String theName = "Partsch Inc";
        OrganisationName name = new OrganisationName(theName, organisation);
        organisation.getNames().add(name);
        organisationRepository.saveAndFlush(organisation);
        // Act
        Organisation theOrganisation = getOne(organisation);
        OrganisationName secondName = new OrganisationName(theName, theOrganisation);
        theOrganisation.getNames().add(secondName);
        // Assert
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            organisationRepository.saveAndFlush(theOrganisation);
        });
    }

    @Test
    @DisplayName("Set Users Organisation Name.")
    public void setUsersOrganisationName() {
        // Arrange
        Organisation organisation = testOrg();
        DocutoolsUser user = testUser(organisation);
        OrganisationName organisationName = organisation.newName("docu tools");
        organisationRepository.save(organisation);
        // Act
        user.setOrganisationName(organisationName);
        userRepository.save(user);
        // Assert
        DocutoolsUser actualUser = userRepository.getOne(user.getId());
        assertThat(actualUser.getOrganisationName(), equalTo(organisationName));
    }

    @NotNull
    private Organisation getOne(Organisation organisation) {
        return organisationRepository.getOne(organisation.getId());
    }

    @NotNull
    private Organisation testOrg() {
        return organisationRepository.save(new Organisation());
    }

    private DocutoolsUser testUser(Organisation organisation) {
        return userRepository.save(new DocutoolsUser(AlternativeFacts.email(), organisation));
    }

}
