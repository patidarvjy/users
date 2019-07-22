package com.docutools.project;

import com.docutools.users.messages.LanguagesLoader;
import com.docutools.contacts.ProjectContact;
import com.docutools.contacts.ProjectContactRepository;
import com.docutools.contacts.ProjectContactService;
import com.docutools.roles.PermissionManager;
import com.docutools.roles.Privilege;
import com.docutools.roles.PrivilegeCheckDTO;
import com.docutools.test.AlternativeFacts;
import com.docutools.users.*;
import com.docutools.users.values.CsvHeaderColumn;
import com.docutools.users.values.UserSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@DisplayName("ProjectContactServiceTest")
public class ProjectContactServiceTest {

    @Autowired
    private ProjectContactService projectContactService;
    @Autowired
    private ProjectContactRepository contactRepository;
    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private LanguagesLoader languagesLoader;

    @MockBean
    private SessionManager sessionManager;
    @MockBean
    private PermissionManager permissionManager;

    @Test
    public void csvProjectContactsGeneratedAsExpected() {
        //GIVEN
        DocutoolsUser currentUser = new DocutoolsUser();
        UserSettings userSettings = new UserSettings();
        userSettings.setLanguage("pl");
        currentUser.setSettings(userSettings);
        Mockito.when(sessionManager.getCurrentUser()).thenReturn(currentUser);

        PrivilegeCheckDTO privilegeCheckDTO = new PrivilegeCheckDTO();
        privilegeCheckDTO.setCheck(true);
        privilegeCheckDTO.setPrivileges(Collections.singletonList(Privilege.ViewTeam));
        Mockito.when(permissionManager.checkPrivilege(Mockito.any(), Mockito.anyList(), Mockito.anyBoolean())).thenReturn(privilegeCheckDTO);

        UUID projectId = UUID.randomUUID();
        for (int i = 0; i < 500; i++) {
            createContact(projectId, false);
            createContact(projectId, true);
        }
        //WHEN
        String contacts = projectContactService.exportProjectContacts(projectId);
        //THEN
        String csvTxt = "First,Last,docutools,first.last@mail.com,123456,,CEO,IT,Seilerstätte,1010,Wien,AT";
        for (int i = 1; i < 500; i++) {
            csvTxt = csvTxt + "\n" + "First,Last,docutools,first.last@mail.com,123456,,CEO,IT,Seilerstätte,1010,Wien,AT";
        }
        String csvHeader = projectContactService.getLocalisedCsvHeader(sessionManager.getCurrentUser().getSettings().getLanguage());
        assertThat(contacts).isEqualTo(csvHeader + "\n" + csvTxt);
    }

    private void createContact(UUID projectId, boolean replaced) {
        ProjectContact contact = new ProjectContact();
        contact.setProjectId(projectId);
        contact.setFirstName("First");
        contact.setLastName("Last");
        contact.setEmail("first.last@mail.com");
        contact.setCity("Wien");
        contact.setCountryCode("AT");
        contact.setCompanyName("docutools");
        contact.setDepartment("IT");
        contact.setPhone("123456");
        contact.setJobTitle("CEO");
        contact.setStreet("Seilerstätte");
        contact.setZip("1010");
        if(replaced) {
            contact.replaceBy(createUser());
        }
        contactRepository.save(contact);
    }

    private DocutoolsUser createUser() {
        Organisation organisation = organisationRepo.save(new Organisation());
        DocutoolsUser user = new DocutoolsUser();
        user.setOrganisation(organisation);
        user.setUsername(AlternativeFacts.email());
        userRepo.save(user);
        return user;
    }
}
