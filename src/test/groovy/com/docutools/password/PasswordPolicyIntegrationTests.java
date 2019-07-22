package com.docutools.password;

import com.docutools.exceptions.ErrorCodes;
import com.docutools.test.AlternativeFacts;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.UserRepo;
import com.docutools.users.resources.RegistrationDTO;
import com.docutools.users.resources.VerificationDTO;
import io.restassured.RestAssured;
import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.*;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Tag("integration")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration","dev"})
public class PasswordPolicyIntegrationTests {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrganisationRepo organisationRepo;
    @Autowired
    private PasswordUserRepoHelper passwordUserRepoHelper;

    @BeforeEach
    public void setup() {
        RestAssured.port = serverPort;
    }

    @Test
    public void requireStrongPasswordPolicyOnVerify() {
        // Arrange
        RegistrationDTO registration = new RegistrationDTO();
        String email = AlternativeFacts.email();
        registration.setEmail(email);
        registration.setFirstName("Max");
        registration.setLastName("Mustermann");
        registration.setCountryCode("at");
        registration.setLanguage("de");
        registration.setOrganisationName("Mustermann AG");

        given()
                .contentType("application/json")
                .body(registration)
        .when()
                .post("/api/v2/register")
        .then()
                .log().all()
                .statusCode(201);

        DocutoolsUser user = activateStrongPolicy(email);

        // Act
        VerificationDTO verification = new VerificationDTO();
        verification.setPassword("unsafePassword");
        verification.setToken(user.getVerificationStatus().getToken());
        given()
                .contentType("application/json")
                .body(verification)
        .when()
                .post("/api/v2/register/verify")
        .then()
                .log().all()
                .statusCode(400)
                .body("code", equalTo(String.format("users-service-%s", ErrorCodes.WEAK_PASSWORD.getCode())));
    }

    @Test
    public void dontAuthenticateUsersWherePasswordIsExpired() throws HeuristicRollbackException, RollbackException, HeuristicMixedException, SystemException, NotSupportedException {
        // Arrange
        RegistrationDTO registration = new RegistrationDTO();
        String email = AlternativeFacts.email();
        registration.setEmail(email);
        registration.setOrganisationName(AlternativeFacts.organisationName());
        registration.setCountryCode(AlternativeFacts.cc());

        given()
                .contentType("application/json")
                .body(registration)
        .when()
                .post("/api/v2/register")
        .then()
                .log().all()
                .statusCode(201);


        DocutoolsUser user = activateStrongPolicy(email);
        VerificationDTO verification = new VerificationDTO();
        String pw = "ThisIsASafePassword1234$%&";
        verification.setPassword(pw);
        verification.setToken(user.getVerificationStatus().getToken());
        given()
                .contentType("application/json")
                .body(verification)
        .when()
                .post("/api/v2/register/verify")
        .then()
                .log().all()
                .statusCode(200);

        // Expire the Password
        passwordUserRepoHelper.expirePassword(user.getId(), LocalDateTime.now().minusYears(1));

        // Act
        given()
                .formParam("username", email)
                .formParam("password", pw)
                .formParam("grant_type", "password")
                .auth().basic("tester", "secret")
        .when()
                .post("/oauth/token")
        .then()
                .log().all()
                .statusCode(400);
    }

    @NotNull
    private DocutoolsUser activateStrongPolicy(String email) {
        DocutoolsUser user = userRepo.findByUsernameIgnoreCase(email)
                .orElseThrow(() -> new AssertionFailedError("User not in DB!"));
        Organisation organisation = user.getOrganisation();
        organisation.setPasswordPolicy("strong");
        organisationRepo.saveAndFlush(organisation);
        return user;
    }

}
