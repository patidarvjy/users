package com.docutools.avatar;

import com.docutools.roles.PrivilegeCheckDTO;
import com.docutools.contacts.ProjectContact;
import com.docutools.users.values.ProfilePicture;
import com.docutools.contacts.ProjectContactRepository;
import com.docutools.users.UserRepo;
import com.docutools.roles.PermissionManager;
import com.docutools.test.DocutoolsTestUser;
import com.docutools.test.TestUserHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class AvatarRequests {

    @LocalServerPort
    private int port;

    @Autowired
    private ProjectContactRepository contactRepository;
    @Autowired
    private UserRepo usersRepository;

    @MockBean
    private PermissionManager permissionManager;

    @Autowired
    private TestUserHelper testUserHelper;
    private DocutoolsTestUser user;
    private String token;

    private ClassPathResource testImage = new ClassPathResource("logo.jpg");

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        user = testUserHelper.newTestUser();
        token = testUserHelper.login(user, port);
        when(permissionManager.checkPrivilege(any(), anyList(), anyBoolean()))
                .then(args -> new PrivilegeCheckDTO(args.getArgument(1), args.getArgument(0), args.getArgument(2), true));
        when(permissionManager.hasPrivileges(any(), any()))
                .thenReturn(true);
    }

    @Test
    @DisplayName("Upload and retrieve an Avatar for a Project Contact.")
    public void uploadAndRetrieveAndAvatar() throws IOException {
        // Arrange
        ProjectContact contact = new ProjectContact(UUID.randomUUID());
        contact.setCompanyName("Toy's 'R Us!");
        contactRepository.save(contact);
        InputStream in = testImage.getInputStream();
        // Act
        given()
                .contentType("multipart/form-data")
                .multiPart("avatar", "avatar.jpg", in, "image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .put("/api/v2/users/{id}/avatar", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200);
        in.close();

        InputStream thumbnailStream = given()
                .accept("image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/users/{id}/avatar", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200)
                .extract().asInputStream();

        BufferedImage thumbnail = ImageIO.read(thumbnailStream);
        thumbnailStream.close();

        InputStream originalStream = given()
                .accept("image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/users/{id}/avatar/original", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200)
                .extract().asInputStream();

        // Assert
        assertThat(thumbnail.getWidth(), is(AvatarService.THUMBNAIL_WIDTH));

        InputStream localStream = testImage.getInputStream();
        BufferedImage local = ImageIO.read(localStream);
        localStream.close();
        BufferedImage original = ImageIO.read(originalStream);
        originalStream.close();
        assertThat(original.getWidth(), equalTo(local.getWidth()));
        assertThat(original.getHeight(), equalTo(local.getHeight()));
    }

    @Test
    @DisplayName("Retrieve Avatar from legacy user.")
    public void retrieveAvatarFromLegacyUsers() throws IOException {
        // Arrange
        ProfilePicture profilePicture = new ProfilePicture();
        profilePicture.setContentType("image/jpeg");
        profilePicture.setOwner(user);
        InputStream in = testImage.getInputStream();
        profilePicture.setData(StreamUtils.copyToByteArray(in));
        in.close();
        user.setAvatar(profilePicture);
        usersRepository.save(user);
        // Act
        given()
                .accept("image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/users/{id}/avatar/original", user.getId()) //
        .then()
                .log().all()
                .statusCode(200);

        given()
                .accept("image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/users/{id}/avatar", user.getId()) //
        .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("Remove personal Avatar.")
    public void removePersonalAvatar() throws IOException {
        // Arrange
        InputStream in = testImage.getInputStream();
        given()
                .contentType("multipart/form-data")
                .multiPart("avatar", "avatar.jpg", in, "image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .put("/api/v2/me/avatar") //
        .then()
                .log().all()
                .statusCode(200);
        in.close();

        // Act
        given()
                .auth().oauth2(token)
                .log().all() //
        .when()
                .delete("/api/v2/me/avatar") //
        .then()
                .log().all()
                .statusCode(200);

        // Assert
        given()
                .accept("image/jpeg")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/me/avatar") //
        .then()
                .log().all()
                .statusCode(204);
    }

}
