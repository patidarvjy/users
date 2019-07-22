package com.docutools.users;

import com.docutools.test.TestUserHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class UsersIntegration {
    @LocalServerPort
    private int port;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestUserHelper testUserHelper;


    @BeforeEach
    public void setup() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Find users whom test licence is expiring in 30 Days")
    public void findTestUserLicenceExpiring() {


        // Arrange
        DocutoolsUser user = testUserHelper.newTestUser();
        testUserHelper.newTestUser(user.getOrganisation());

        List<DocutoolsUser> userLicenceExpiring =
            userRepo.findTestUserLicenceExpiring(LocalDate.now().plusDays(30), LocalDate.now().plusDays(30));

        Assertions.assertEquals(2, userLicenceExpiring.size());

    }

    @Test
    @DisplayName("UnSubscribe emails")
    public void UnSubscribeEmails() {


        // Arrange
        DocutoolsUser user = testUserHelper.newTestUser();

        given()
            .contentType("application/json")
            .accept("application/json")
            .queryParam("userId", user.getId())
            .log().all()
            .when()
            .get("/api/v2/users/unsubscribe")
            .then()
            .log().all()
            .statusCode(200);

        Assertions.assertTrue(!userRepo.findById(user.getId()).get().getEmailSubscribed());

    }

}
