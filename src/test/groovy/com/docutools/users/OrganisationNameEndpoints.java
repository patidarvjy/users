package com.docutools.users;

import com.docutools.test.DocutoolsTestUser;
import com.docutools.test.TestUserHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class OrganisationNameEndpoints {

    @LocalServerPort
    private int localServerPort;
    @Autowired
    private TestUserHelper testUserHelper;
    @Autowired
    private OrganisationNameService nameService;
    @Autowired
    private UserRepo userRepo;
    @MockBean
    private SessionManager sessionManager;

    private DocutoolsTestUser user;
    private String token;

    @BeforeEach
    public void setup() {
        RestAssured.port = localServerPort;
        this.user = testUserHelper.newTestUser();
        this.token = testUserHelper.login(this.user, localServerPort);
        Mockito.when(sessionManager.getCurrentUser())
                .thenReturn(this.user);
    }

    @Test
    @DisplayName("Create a new name.")
    public void createANewName() {
        OrganisationName name = new OrganisationName("docu tools");
        given()
                .contentType("application/json")
                .body(name)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .post("/api/v2/me/organisation/names")
        .then()
                .log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("Fail when new Name already exists.")
    public void failWhenNameAlreadyExists() {
        createName("docu tools");
        given()
                .contentType("application/json")
                .body(new OrganisationName("docu tools"))
                .accept("application/json")
                .auth().oauth2(token)
        .when()
                .post("/api/v2/me/organisation/names")
        .then()
                .log().all()
                .statusCode(409);
    }


    @Test
    @DisplayName("List organisation names.")
    public void listNames() {
        createName("docu tools");
        createName("Sustain Solutions");
        given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .get("/api/v2/me/organisation/names")
        .then()
                .log().all()
                .statusCode(200)
                .body("$.size()", is(3));
    }

    @Test
    @DisplayName("Get name.")
    public void getName() {
        OrganisationName expected = createName("docu tools");
        OrganisationName actual = given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .get("/api/v2/me/organisation/names/{id}", expected.getId())
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(OrganisationName.class);
        assertThat(actual, equalTo(expected));
    }

    @Test
    @DisplayName("Change name.")
    public void changeName() {
        OrganisationName name = createName("docu tools");
        given()
                .contentType("application/json")
                .body(new OrganisationName("docu tools GmbH"))
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .put("/api/v2/me/organisation/names/{id}", name.getId())
        .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("docu tools GmbH"));
    }

    @Test
    @DisplayName("Fail when Name Update already exists.")
    public void failWhenNameUpdateAlreadyExists() {
        createName("docu tools GmbH");
        OrganisationName name = createName("docu tools");
        given()
                .contentType("application/json")
                .body(new OrganisationName("docu tools GmbH"))
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .put("/api/v2/me/organisation/names/{id}", name.getId())
        .then()
                .log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("Delete name.")
    public void deleteName() {
        OrganisationName name = createName("docu tools");
        given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
         .when()
                .delete("/api/v2/me/organisation/names/{id}", name.getId())
         .then()
                .log().all()
                .statusCode(200);
        // Assert
        given()
                .accept("application/json")
                .auth().oauth2(token)
        .when()
                .get("/api/v2/me/organisation/names/{id}", name.getId())
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Cannot delete Names in Use.")
    public void cannotDeleteName() {
        OrganisationName name = createName("docu tools");
        user.setOrganisationName(name);
        userRepo.saveAndFlush(user);
        given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .delete("/api/v2/me/organisation/names/{id}", name.getId())
        .then()
                .log().all()
                .statusCode(400);
    }

    private OrganisationName createName(String name) {
        return given()
                .contentType("application/json")
                .body(new OrganisationName(name))
                .auth().oauth2(token)
        .when()
                .post("/api/v2/me/organisation/names")
        .then()
                .statusCode(201)
                .extract().as(OrganisationName.class);
    }

}
