package com.docutools.users;

import com.docutools.saml.SAMLAuthenticationRequest;
import com.docutools.saml.SAMLTokenService;
import com.docutools.test.AlternativeFacts;
import com.docutools.test.DocutoolsTestUser;
import com.docutools.test.TestUserHelper;
import com.docutools.users.resources.ChangeEmailDTO;
import com.docutools.users.resources.ChangePasswordDTO;
import io.restassured.RestAssured;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class SamlTestCases {

    @LocalServerPort
    private int localServerPort;
    @Autowired
    private TestUserHelper testUserHelper;
    @Autowired
    private OrganisationManager organisationManager;

    @Autowired
    private OrganisationRepo organisationRepo;
    @MockBean
    private SessionManager sessionManager;
    @Autowired
    private SAMLTokenService samlTokenService;

    @Value("${docutools.saml.private.keyPath}")
    private String keyPath;

    private DocutoolsTestUser user;
    private String token;

    @BeforeEach
    public void setup() {
        RestAssured.port = localServerPort;
        this.user = testUserHelper.newSAMLUser();
        Mockito.when(sessionManager.getCurrentUser())
            .thenReturn(this.user);
    }

    @Test
    @DisplayName("Authorize new SAML user.")
    public void authorizeSamlUser() {

        String idp = AlternativeFacts.randomString();

        Organisation org = new Organisation();
        org.setName(AlternativeFacts.organisationName());
        org.setCc(AlternativeFacts.cc());
        org.setIdpLink(idp);
        organisationRepo.save(org);

        String email = AlternativeFacts.email();
        String signature = signData(String.format("%s +++ %s", email, idp));

        SAMLAuthenticationRequest authenticationRequest = new SAMLAuthenticationRequest(null, email, idp, signature,
            "FirstName", "LastName", null, null);

        given()
            .contentType("application/json")
            .body(authenticationRequest)
            .accept("application/json")
            .log().all()
            .when()
            .post("/saml/token")
            .then()
            .log().all()
            .statusCode(200);
    }

    private String signData(String data) {
        try {
            Signature sig = Signature.getInstance("SHA1WithRSA");
            sig.initSign(getKeyPair());
            sig.update(data.getBytes("UTF8"));
            byte[] signatureBytes = sig.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private PrivateKey getKeyPair() throws NoSuchAlgorithmException {
        try {
            byte[] pubKey;
            FileSystemResource fileSystemResource = new FileSystemResource(this.keyPath);
            try (PemReader reader = new PemReader(new InputStreamReader(fileSystemResource.getInputStream()))) {
                pubKey = reader.readPemObject().getContent();
            }
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(pubKey);
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (IOException | InvalidKeySpecException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private OAuth2AccessToken samlTestUesr() {
        String idp = AlternativeFacts.randomString();

        Organisation org = new Organisation();
        org.setName(AlternativeFacts.organisationName());
        org.setCc(AlternativeFacts.cc());
        org.setIdpLink(idp);
        organisationRepo.save(org);

        String email = AlternativeFacts.email();
        String signature = signData(String.format("%s +++ %s", email, idp));

        SAMLAuthenticationRequest authenticationRequest = new SAMLAuthenticationRequest(null, email, idp, signature,
            "FirstName", "LastName", null, null);
        return samlTokenService.authorize(authenticationRequest);
    }

    @Test
    @DisplayName("Not allow SAML user to change email.")
    public void notAllowSAMLUserToChangeEmail() {

        ChangeEmailDTO body = new ChangeEmailDTO(AlternativeFacts.randomPassword(), AlternativeFacts.email());
        given()
            .contentType("application/json")
            .body(body)
            .auth().oauth2(samlTestUesr().getValue())
            .accept("application/json")
            .log().all()
            .when()
            .put("/api/v2/me/email")
            .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("Not allow SAML user to change password.")
    public void notAllowSAMLUserToChangePassword() {

        ChangePasswordDTO body = new ChangePasswordDTO(AlternativeFacts.randomPassword(), AlternativeFacts.randomPassword());
        given()
            .contentType("application/json")
            .body(body)
            .auth().oauth2(samlTestUesr().getValue())
            .accept("application/json")
            .log().all()
            .when()
            .put("/api/v2/me/password")
            .then()
            .log().all()
            .statusCode(400);
    }

    @Test
    @DisplayName("Only allow SAML users to Authorize via SAML endpoint.")
    public void onlyAllowSamlUserToAuthorizeViaSAMLEndpointSamlUser() {

        String idp = AlternativeFacts.randomString();

        Organisation org = new Organisation();
        org.setName(AlternativeFacts.organisationName());
        org.setCc(AlternativeFacts.cc());
        org.setIdpLink(idp);
        organisationRepo.save(org);

        String email = testUserHelper.newTestUser().getEmail();
        String signature = signData(String.format("%s +++ %s", email, idp));

        SAMLAuthenticationRequest authenticationRequest = new SAMLAuthenticationRequest(null, email, idp, signature,
            "FirstName", "LastName", null, null);

        given()
            .contentType("application/json")
            .body(authenticationRequest)
            .accept("application/json")
            .log().all()
            .when()
            .post("/saml/token")
            .then()
            .log().all()
            .statusCode(401);
    }
}
