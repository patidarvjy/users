package com.docutools.oauth2;

import com.docutools.roles.Privilege;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@Tag("integration")
public class ClientCredentialsIntegration {

    @LocalServerPort
    private int localServerPort;
    private String customerId = "790140fb-609b-4f68-8078-a35586aa0033";
    private String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdhbml6YXRpb25JZCI6ImI0YmJhOWYxLTM3MjEtNGYxZC1hYTcxLTM2ZTI5YWRhNjU5YSIsImZpcnN0TmFtZSI6IkFsZXhhbmRlciIsImxhc3ROYW1lIjoiUGFydHNjaCIsInVzZXJfbmFtZSI6ImFsZXhhbmRlckBwYXJ0c2NoLm5pbmphIiwic2NvcGUiOlsidXNlcnMiLCJvcmdhbmlzYXRpb25zIl0sImlkIjoiN2JmYjQxY2MtZDgyNS00YjEyLWJkZTQtMDVjZjBlNjlhYjc5IiwiZXhwIjoxNTQyNzY2NzM4LCJhdXRob3JpdGllcyI6WyJvd25lciIsImFkbWluIiwic3VzdGFpbl91c2VyIl0sImp0aSI6ImUxNDhhY2JlLTZjYWMtNDJjNC1hYzRhLTYwZTJhOTQ5NjY0MiIsImVtYWlsIjoiYWxleGFuZGVyQHBhcnRzY2gubmluamEiLCJjbGllbnRfaWQiOiJ0ZXN0ZXIifQ.pRijp9ixni9jpbq_5Srf3XRw36WMob14ZCDKa7Raky2fntfcSguS5QAXflDeGqQqNi_CKMtVZz9szj6zTVCyGzvAndXy_PXBJurdQE93ngQxjPPTLgAPT4NZNpCS2EWWjZAhgNG7iTme-WqTgh04fMZNj2rB9kWA51PFuwgLHYff6U-a1_BTsD4qcaWSPnqASAVuYw8kk1q6y55WJUHfd0T5BAtKeeNqzqAIxDCuXV0g_cfkQvOMp_8CW_5kvCDvtcdnKQ-fHTzsZXjvLInNi_xj8W72Cr5IfzrRhuQuTAG_Uq8F-kSzyzMyokLexSLmvCkVn-GyFbZ96VantQtURQ";
    private UUID cusotmerWithCCId = UUID.fromString("9aaec708-48df-4a5a-b678-73d3353837b1");
    private String customerWithCCUsername = "devin.ye.lin@gmail.com";
    private UUID customerWithCCProjectId = UUID.fromString("0a3f78f2-1e2f-4718-9035-7e1a14319d30");


    @BeforeEach
    public void beforeEach() {
        RestAssured.port = localServerPort;
    }

    @Test
    @DisplayName("Query if the customers have client credentials.")
    public void queryIfCustomersHaveClientCredentials() {
        // Act
        boolean hasClientCrednetials = given()
                .accept("application/json")
                .auth().oauth2(accessToken)
                .when().get("/api/v2/customers/{id}", customerId)
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("hasClientCredentials");
        // Assert
        Assertions.assertFalse(hasClientCrednetials);
    }

    @Test
    @DisplayName("Renew client credentials.")
    public void renewClientCredentials() {
        // Assert
        DocuToolsClientCredentials credentials = given()
                .contentType("application/json")
                .body(new RenewCredentialsRequest(cusotmerWithCCId))
                .accept("application/json")
                .auth().oauth2(accessToken)
                .when().put("/client-credentials-api/v1/credentials")
                .then()
                .log().all()
                .statusCode(201)
                .extract().as(DocuToolsClientCredentials.class);

        String clientCredentialsAccessToken = given()
                .accept("application/json")
                .formParam("grant_type", "client_credentials")
                .auth().basic(credentials.getClientId(), credentials.getClientSecret())
                .when().post("/oauth/token")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("access_token");

        // Assert

        // Check if flag in Customer is now true
        given()
                .accept("application/json")
                .auth().oauth2(accessToken)
                .when().get("/api/v2/customers/{id}", cusotmerWithCCId)
                .then()
                .log().all()
                .statusCode(200)
                .body("hasClientCredentials", Matchers.is(true));

        // Request /me as Owner
        given()
                .accept("application/json")
                .auth().oauth2(clientCredentialsAccessToken)
                .when().get("/api/v2/me")
                .then()
                .log().all()
                .statusCode(200)
                .body("username", Matchers.equalTo(customerWithCCUsername));

        given()
                .accept("application/json")
                .queryParam("projectId", customerWithCCProjectId)
                .queryParam("privilege", Privilege.CloseTasks)
                .auth().oauth2(clientCredentialsAccessToken)
                .when().get("/api/v2/me/checkPrivilege")
                .then()
                .log().all()
                .statusCode(200);
    }

}
