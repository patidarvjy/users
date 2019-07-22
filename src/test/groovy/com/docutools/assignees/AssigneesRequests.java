package com.docutools.assignees;

import com.docutools.users.DocutoolsUser;
import com.docutools.team.MembershipState;
import com.docutools.contacts.ProjectContact;
import com.docutools.team.TeamMembership;
import com.docutools.contacts.ProjectContactRepository;
import com.docutools.team.TeamMembershipRepo;
import com.docutools.roles.PermissionManager;
import com.docutools.test.DocutoolsTestUser;
import com.docutools.test.TestUserHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.trustStore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class AssigneesRequests {

    @LocalServerPort
    private int port;

    @Autowired
    private ProjectContactRepository contactRepository;
    @Autowired
    private TeamMembershipRepo membershipRepository;

    @MockBean
    private PermissionManager permissionManager;

    @Autowired
    private TestUserHelper testUserHelper;
    private DocutoolsTestUser user;
    private String token;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        user = testUserHelper.newTestUser();
        token = testUserHelper.login(user, port);
        when(permissionManager.isMember(any())).thenReturn(true);
    }

    @Test
    @DisplayName("List all Assignees.")
    public void listAllAssignees() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        // Team Members from Same Company
        DocutoolsUser userA = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(userA, projectId, MembershipState.Active));
        DocutoolsUser userB = testUserHelper.newTestUser(userA.getOrganisation());
        membershipRepository.save(new TeamMembership(userB, projectId, MembershipState.Active));

        DocutoolsUser userC = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(userC, projectId, MembershipState.Active));

        DocutoolsUser removedMember = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(removedMember, projectId, MembershipState.Removed));

        ProjectContact contact1 = new ProjectContact(projectId);
        contact1.setCompanyName("Teuner AV");
        contactRepository.save(contact1);

        ProjectContact contact2 = new ProjectContact(projectId);
        contact2.setFirstName("Maximilian Vladimir");
        contact2.setLastName("Allmayer-Beck");
        contactRepository.save(contact2);

        // Act
        Assignee[] assignees = given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/projects/{projectId}/assignees/all", projectId) //
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(Assignee[].class);

        // Assert
        assertThat(Arrays.asList(assignees), containsInAnyOrder(new Assignee(userA), new Assignee(userB), new Assignee(userC), new Assignee(contact1), new Assignee(contact2)));
        assertThat(assignees.length, is(5));
    }

    @Test
    @DisplayName("List User Assignees from Company.")
    public void listUserAssigneesFromCompany() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser userA = testUserHelper.newTestUser();
        UUID company = userA.getOrganisation().getId();
        membershipRepository.save(new TeamMembership(userA, projectId, MembershipState.Active));
        DocutoolsUser userB = testUserHelper.newTestUser(userA.getOrganisation());
        membershipRepository.save(new TeamMembership(userB, projectId, MembershipState.Active));

        DocutoolsUser userC = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(userC, projectId, MembershipState.Active));

        ProjectContact contact = new ProjectContact(projectId);
        contact.setCompanyName("Toys 'R Us");
        contactRepository.save(contact);

        // Act
        Assignee[] assignees = given()
                .accept("application/json")
                .queryParam("company", company)
                .auth().oauth2(token)
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/assignees/all", projectId)
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(Assignee[].class);

        // Assert
        assertThat(Arrays.asList(assignees), containsInAnyOrder(new Assignee(userA), new Assignee(userB)));
        assertThat(assignees.length, is(2));
    }

    @Test
    @DisplayName("List Contact Assignees from Company.")
    public void listContactAssigneesFromCompany() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser removedMember = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(removedMember, projectId, MembershipState.Removed));

        ProjectContact contact1 = new ProjectContact(projectId);
        contact1.setCompanyName("Teuner AV");
        contactRepository.save(contact1);

        ProjectContact contact2 = new ProjectContact(projectId);
        contact2.setFirstName("Maximilian Vladimir");
        contact2.setLastName("Allmayer-Beck");
        contactRepository.save(contact2);

        // Act
        Assignee[] assignees = given()
                .accept("application/json")
                .queryParam("company", contact1.getId())
                .auth().oauth2(token)
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/assignees/all", projectId)
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(Assignee[].class);

        // Assert
        assertThat(Arrays.asList(assignees), contains(new Assignee(contact1)));
        assertThat(assignees.length, is(1));

    }

    @Test
    @DisplayName("List companies.")
    public void listCompanies() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser userA = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(userA, projectId, MembershipState.Active));
        DocutoolsUser userB = testUserHelper.newTestUser(userA.getOrganisation());
        membershipRepository.save(new TeamMembership(userB, projectId, MembershipState.Active));

        DocutoolsUser userC = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(userC, projectId, MembershipState.Active));

        DocutoolsUser removedMember = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(removedMember, projectId, MembershipState.Removed));

        ProjectContact contact1 = new ProjectContact(projectId);
        contact1.setCompanyName("Teuner AV");
        contactRepository.save(contact1);

        ProjectContact contact2 = new ProjectContact(projectId);
        contact2.setFirstName("Maximilian Vladimir");
        contact2.setLastName("Allmayer-Beck");
        contactRepository.save(contact2);

        // Act
        AssigneeCompany[] companies = given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/companies/all", projectId)
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(AssigneeCompany[].class);

        // Assert
        assertThat(Arrays.asList(companies), containsInAnyOrder(new AssigneeCompany(userA.getOrganisation(), userA), new AssigneeCompany(userC.getOrganisation(), userC),
                new AssigneeCompany(contact1), new AssigneeCompany(contact2)));
    }

    @Test
    @DisplayName("Include removed assignees.")
    public void includeRemovedAssignees() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser userA = testUserHelper.newTestUser();
        membershipRepository.save(new TeamMembership(userA, projectId, MembershipState.Active));
        DocutoolsUser userB = testUserHelper.newTestUser(userA.getOrganisation());
        membershipRepository.save(new TeamMembership(userB, projectId, MembershipState.Removed));


        // Act
        Assignee[] assignees = given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/assignees/all", projectId)
        .then()
                .log().all()
                .statusCode(200)
                .extract().as(Assignee[].class);

        assertThat(Arrays.asList(assignees), containsInAnyOrder(new Assignee(userA, MembershipState.Active),
                new Assignee(userB, MembershipState.Removed)));
    }

}
