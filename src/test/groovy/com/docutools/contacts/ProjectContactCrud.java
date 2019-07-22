package com.docutools.contacts;

import com.docutools.test.AlternativeFacts;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Transactional
public class ProjectContactCrud {

    @Autowired
    private ProjectContactRepository projectContactRepository;
    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private UserRepo userRepo;

    @Test
    public void createEmptyContact() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        ProjectContact contact = new ProjectContact(projectId);
        // Act
        projectContactRepository.saveAndFlush(contact);
        ProjectContact actual = projectContactRepository.getOne(contact.getId());
        // Assert
        assertThat(actual, equalTo(contact));
    }

    @Test
    public void createContact() {
        // Arrange
        ProjectContact contact = new ProjectContact(UUID.randomUUID());
        contact.setEmail("m.allmayer-beck@docu-tools.com");
        contact.setCompanyName("docu tools GmbH");
        contact.setFirstName("Maximilian Validimir");
        contact.setLastName("Allmayer-Beck");
        contact.setStreet("Reisnergasse 2");
        contact.setZip("1130");
        contact.setCity("Vienna");
        contact.setPhone("066413372345");
        contact.setFax("+11239982");
        contact.setInternalId("X01X");
        contact.setDepartment("Sales");
        contact.setJobTitle("Head of Sales");
        // Act
        projectContactRepository.saveAndFlush(contact);
        ProjectContact actual = projectContactRepository.getOne(contact.getId());
        // Assert
        assertThat(actual, equalTo(contact));
    }

    @Test
    public void updateContact() {
        // Arrange
        ProjectContact contact = projectContactRepository.saveAndFlush(new ProjectContact(UUID.randomUUID()));
        contact.setFirstName("Alexander");
        contact.setLastName("Partsch");
        // Act
        projectContactRepository.saveAndFlush(contact);
        ProjectContact actual = projectContactRepository.getOne(contact.getId());
        // Assert
        assertThat(actual, equalTo(contact));
    }

    @Test
    public void deleteContact() {
        // Arrange
        ProjectContact contact = projectContactRepository.saveAndFlush(new ProjectContact(UUID.randomUUID()));
        // Act
        projectContactRepository.delete(contact);
        // Assert
        assertThat(projectContactRepository.existsById(contact.getId()), is(false));
    }

    @Test
    public void findByExample() {
        // Arrange
        ProjectContact contact = new ProjectContact(UUID.randomUUID());
        contact.setFirstName("Wendelin");
        contact.setLastName("Peleska");
        contact.setPhone("0699123123");
        contact.setJobTitle("Lead Web Developer");
        projectContactRepository.saveAndFlush(contact);
        ProjectContact probe = new ProjectContact();
        probe.setLastName("Peleska");
        probe.setPhone("0699123123");
        // Act
        Example<ProjectContact> example = Example.of(contact);
        ProjectContact actual = projectContactRepository.findOne(example).orElse(null);
        // Assert
        assertThat(actual, notNullValue());
        assertThat(actual, equalTo(contact));
    }

    @Test
    public void filterInReplacedContacts() {
        // Arrange
        Organisation organisation = organisationRepo.save(new Organisation());
        DocutoolsUser user = new DocutoolsUser();
        user.setOrganisation(organisation);
        user.setUsername(AlternativeFacts.email());
        userRepo.save(user);

        ProjectContact replacedContact = new ProjectContact(UUID.randomUUID());
        replacedContact.replaceBy(user);
        projectContactRepository.save(replacedContact);

        // Act
        List<ProjectContact> contacts = projectContactRepository.searchInProject(replacedContact.getProjectId(), "")
                .collect(Collectors.toList());

        // Assert
        assertThat(contacts, empty());
    }

    @Test
    public void searchContactInProject() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        int page = 0;
        int size = 10;
        for (int i = 0; i < 15; i++) {
            ProjectContact contact = new ProjectContact(projectId);
            contact.setFirstName("Wendelin");
            contact.setLastName("Peleska");
            contact.setPhone("0699123123");
            contact.setJobTitle("Lead Web Developer");
            contact.setProjectId(projectId);
            projectContactRepository.saveAndFlush(contact);
        }

        // WHEN
        List<ProjectContact> projectContacts = projectContactRepository.searchInProject(projectId, "Web", PageRequest.of(page, size)).collect(Collectors.toList());

        // THEN
        assert projectContacts.size() == size;
    }
}
