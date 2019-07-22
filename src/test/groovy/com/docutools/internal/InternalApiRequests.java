package com.docutools.internal;

import com.docutools.team.MembershipState;
import com.docutools.team.TeamMembership;
import com.docutools.team.TeamMembershipRepo;
import com.docutools.test.DocutoolsTestUser;
import com.docutools.test.TestUserHelper;
import com.docutools.users.DocutoolsUser;
import io.restassured.RestAssured;
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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class InternalApiRequests {

    @LocalServerPort
    private int port;

    @Autowired
    private TeamMembershipRepo membershipRepository;

    @Autowired
    private TestUserHelper testUserHelper;
    private DocutoolsTestUser user;
    private String token;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        user = testUserHelper.newTestUser();
        token = testUserHelper.login(user, port);
    }

    @Test
    @DisplayName("Check is Member.")
    public void checkIsMember() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser userA = testUserHelper.newTestUser();

        membershipRepository.save(new TeamMembership(userA, projectId, MembershipState.Active));


        // Act
        Boolean isMember = given()
            .accept("application/json")
            .auth().oauth2(token)
            .queryParam("userId", userA.getId())
            .queryParam("projectId", projectId)
            .log().all() //
            .when()
            .get("/api/internal/v2/checkMember")
            .then()
            .log().all()
            .statusCode(200)
            .extract().as(Boolean.class);

        // Assert
        assertThat("Is Member", isMember);
    }

}
