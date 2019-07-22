package com.docutools.contacts;

import com.docutools.roles.PermissionManager;
import com.docutools.roles.PrivilegeCheckDTO;
import com.docutools.test.TestUserHelper;
import com.docutools.users.ImportedFileDTO;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class ProjectContactRequests {

    @LocalServerPort
    private int port;

    @Autowired
    private ProjectContactRepository contactRepository;

    @MockBean
    private PermissionManager permissionManager;

    @Autowired
    private TestUserHelper testUserHelper;
    private String token;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        token = testUserHelper.login(testUserHelper.newTestUser(), port);
        when(permissionManager.checkPrivilege(any(), anyList(), anyBoolean()))
                .then(args -> new PrivilegeCheckDTO(args.getArgument(1), args.getArgument(0), args.getArgument(2), true));
        when(permissionManager.hasPrivileges(any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("Create and Retrieve new Project Contact.")
    public void createAndRetrieveContact() {
        // Arrange
        ProjectContact resource = new ProjectContact();
        resource.setCompanyName("Hinteralm EU");
        resource.setEmail("peter@hinteralm.at");
        resource.setFirstName("Peter");
        resource.setLastName("Hinteralminger");
        resource.setJobTitle("Barman");
        resource.setDepartment("Bar");
        resource.setInternalId("Bx01");
        resource.setStreet("Stolberggasse 27/12");
        resource.setZip("1050");
        resource.setCity("Vienna");
        resource.setPhone("011337892");
        resource.setFax("011337893");
        resource.setProjectId(UUID.randomUUID());

        // Act
        ProjectContact created = given()
                .contentType("application/json")
                .body(resource)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .post("/api/v2/contacts") //
        .then()
                .log().all()
                .statusCode(201)
                .extract().as(ProjectContact.class);

        assertThat(created, notNullValue());
        assertThat(created.getId(), notNullValue());

        resource.setId(created.getId());
        ProjectContact retrieved = given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/contacts/{id}", resource.getId()) //
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(ProjectContact.class);

        // Assert
        assertThat(retrieved, equalTo(resource));
    }

    @Test
    @DisplayName("Update Project Contact")
    public void updateProjectContact() {
        // Arrange
        ProjectContact resource = new ProjectContact(UUID.randomUUID());
        resource.setEmail("tonypolster@fak.at");
        ProjectContact contact = contactRepository.save(resource);

        // Act
        ProjectContact update = new ProjectContact();
        update.setFirstName("Tony");
        update.setLastName("Polster");
        ProjectContact updated = given()
                .contentType("application/json")
                .body(update)
                .auth().oauth2(token)
                .log().all() //
        .when()
                .patch("/api/v2/contacts/{id}", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(ProjectContact.class);

        // Assert
        assertThat(updated.getFirstName(), equalTo("Tony"));
        assertThat(update.getLastName(), equalTo("Polster"));
    }

    @Test
    @DisplayName("Delete Project Contact")
    public void deleteProjectContact() {
        // Arrange
        ProjectContact resource = new ProjectContact(UUID.randomUUID());
        resource.setCompanyName("Porr AG");

        ProjectContact contact = contactRepository.saveAndFlush(resource);

        // Act
        given()
                .auth().oauth2(token)
                .log().all() //
        .when()
                .delete("/api/v2/contacts/{id}", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200);

        // Assert
        assertThat(contactRepository.existsById(contact.getId()), is(false));
    }

    @Test
    @DisplayName("Search Projects Contacts")
    public void searchProjectsContacts() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        ProjectContact sarahWiener = new ProjectContact(projectId);
        sarahWiener.setFirstName("Sarah");
        sarahWiener.setLastName("Wiener");

        ProjectContact arminWolf = new ProjectContact(projectId);
        arminWolf.setFirstName("Armin");
        arminWolf.setLastName("Wolf");
        arminWolf.setCity("Wien");

        ProjectContact hermannMayer = new ProjectContact(projectId);
        hermannMayer.setFirstName("Hermann");
        hermannMayer.setLastName("Mayer");

        List<ProjectContact> contacts = Arrays.asList(sarahWiener, arminWolf, hermannMayer);
        contactRepository.saveAll(contacts);

        // Act
        ProjectContact[] projectContacts = given()
                .accept("application/json")
                .queryParam("projectId", projectId)
                .queryParam("search", "wien")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/contacts") //
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(ProjectContact[].class);

        // Assert
        assertThat(projectContacts, notNullValue());
        assertThat(projectContacts.length, is(2));
        assertThat(Arrays.asList(projectContacts), containsInAnyOrder(sarahWiener, arminWolf));
    }

    @Test
    @DisplayName("Export Contact as VCard")
    public void exportVCard(){
        // Arrange
        ProjectContact resource = new ProjectContact();
        resource.setCompanyName("Hinteralm EU");
        resource.setEmail("peter@hinteralm.at");
        resource.setFirstName("Peter");
        resource.setLastName("Hinteralminger");
        resource.setJobTitle("Barman");
        resource.setDepartment("Bar");
        resource.setInternalId("Bx01");
        resource.setStreet("Stolberggasse 27/12");
        resource.setZip("1050");
        resource.setCity("Vienna");
        resource.setPhone("011337892");
        resource.setFax("011337893");
        resource.setProjectId(UUID.randomUUID());

        contactRepository.save(resource);


        String vCard = given()
                .accept("text/x-vcard")
                .auth().oauth2(token)
                .log().all() //
                .when()
                .get("/api/v2/contacts/{id}/vcard", resource.getId())
                .then()
                .log().all()
                .statusCode(200)
                .extract().asString();

        assertNotNull(vCard);
    }

    @Test
    @DisplayName("Import Project Contact Upload File.")
    public void importContact() {

        ClassPathResource classPathResource = new ClassPathResource("contacts.csv");
        File file= null;
        try {
            file = classPathResource.getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Act
        ImportedFileDTO importedFileDTO = given()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("file", file, "text/csv")
            .accept("application/json")
            .auth().oauth2(token)
            .queryParam("delimiter", ";")
            .queryParam("projectId", UUID.randomUUID())
            .log().all() //
            .when()
            .post("/api/v2/contacts/import/file") //
            .then()
            .log().all()
            .statusCode(200)
            .extract().as(ImportedFileDTO.class);

        assertThat(importedFileDTO, notNullValue());
        assertThat(importedFileDTO.getId(), notNullValue());
        assertEquals (importedFileDTO.getColumns().length,3);

        Map<String,Columns> columnsMap = new HashMap<>();
        columnsMap.put("email",Columns.Email);
        columnsMap.put("first",Columns.FirstName);
        columnsMap.put("company",Columns.CompanyName);
        ProjectContactImport projectContactImport = new ProjectContactImport(importedFileDTO.getId(),UUID.randomUUID(), ';',columnsMap,"UTF-8");

        // Act
        ProjectContact[] projectContacts = given()
            .contentType("application/json")
            .accept("application/json")
            .auth().oauth2(token)
            .body(projectContactImport)
            .log().all() //
            .when()
            .post("/api/v2/contacts/import") //
            .then()
            .log().all()
            .statusCode(200)
            .extract().as(ProjectContact[].class);
        assertNotNull(projectContacts);
        assertEquals(projectContacts.length,3);
    }

    @Test
    @DisplayName("Deny a Contact CSV File with line breaks in Header Row.")
    public void denyAContactCSVWithLineBReaksInHeaderRow() throws IOException {
        ClassPathResource csvFile = new ClassPathResource("invalid_contacts.csv");
        // Act
        given()
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .multiPart("file", csvFile.getFile(), "application/csv")
                .queryParam("delimiter", ";")
                .queryParam("projectId", UUID.randomUUID())
                .auth().oauth2(token)
                .log().all()
        .when()
            .post("/api/v2/contacts/import/file")
        .then()
            .log().all()
            .statusCode(400)
            .body("code", Matchers.equalTo("LINE_BREAK_IN_HEADER_ROW"));

    }
}
