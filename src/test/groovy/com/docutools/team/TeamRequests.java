package com.docutools.team;

import com.docutools.roles.RoleRepo;
import com.docutools.roles.*;
import com.docutools.contacts.ProjectContact;
import com.docutools.users.DocutoolsUser;
import com.docutools.contacts.ProjectContactRepository;
import com.docutools.team.TeamMembershipRepo;
import com.docutools.services.projects.ProjectApiClient;
import com.docutools.services.projects.resources.Project;
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

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class TeamRequests {
    @LocalServerPort
    private int port;

    @Autowired
    private ProjectContactRepository contactRepository;
    @Autowired
    private TeamMembershipRepo membershipRepository;

    @MockBean
    private PermissionManager permissionManager;
    @MockBean
    private ProjectApiClient projectApiClient;

    @Autowired
    private RoleManager roleManager;
    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private TestUserHelper testUserHelper;
    private DocutoolsTestUser user;
    private String token;

    @BeforeEach
    public void setup() {
        RestAssured.port = port;
        user = testUserHelper.newOwner();
        token = testUserHelper.login(user, port);
        when(permissionManager.isMember(any())).thenReturn(true);
        when(permissionManager.checkPrivilege(any(), anyList(), anyBoolean()))
                .then(args -> new PrivilegeCheckDTO(args.getArgument(1), args.getArgument(0), args.getArgument(2), true));
        when(projectApiClient.getProject(any()))
                .then(args -> {
                    Project project = new Project();
                    project.setId(args.getArgument(0));
                    project.setOrganisationId(user.getOrganisation().getId());
                    return project;
                });
    }

    @Test
    @DisplayName("Get single role for legacy roles attribute.")
    public void getSingleRole() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        DocutoolsUser user = testUserHelper.newTestUser();

        TeamMembership membership = new TeamMembership(user, projectId, MembershipState.Active);
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);
        roleRepository.saveAll(roles);
        membership.setRoles(new HashSet<>(roles));
        membershipRepository.save(membership);

        // Act
        given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .get("/api/v2/projects/{projectId}/team", projectId)//
        .then()
                .log().all()
                .statusCode(200)
                .body("[0].role.type", equalTo(RoleType.PowerUser.toString()));
    }

    @Test
    @DisplayName("Update Single Roles.")
    public void updateSingleRoles() {
        // Arrange
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);
        roleRepository.saveAll(roles);

        DocutoolsUser user = testUserHelper.newTestUser();
        TeamMemberDTO body = new TeamMemberDTO();
        body.setUserId(user.getId());
        body.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toSet()));
        body.setRoleId(viewer.getId());

        // Act
        TeamMemberDTO newMember = given()
                .contentType("application/json")
                .body(body)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .post("/api/v2/projects/{projectId}/team", UUID.randomUUID()) //
        .then()
                .log().all()
                .statusCode(201)
                .extract().as(TeamMemberDTO.class);

        // Assert
        assertThat(newMember.getRole().getId(), equalTo(viewer.getId()));
        assertThat(newMember.getRoles().size(), is(1));
        assertThat(newMember.getRoles().stream().findFirst().map(RoleDTO::getId).orElse(null), equalTo(viewer.getId()));
    }

    @Test
    @DisplayName("Invite new Project Contact to Team.")
    public void inviteNewContactToTeam() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        ProjectContact contact = new ProjectContact(projectId);
        contact.setEmail("ferdinand.stiller@example.org");
        contact.setFirstName("Ferdinand");
        contact.setLastName("Stiller");
        contactRepository.save(contact);

        TeamMemberDTO body = new TeamMemberDTO();
        body.setUserId(contact.getId());

        // Act
        TeamMemberDTO newMember = given()
                .contentType("application/json")
                .body(body)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all() //
        .when()
                .post("/api/v2/projects/{projectId}/team", projectId) //
        .then()
                .log().all()
                .statusCode(201)
                .extract().as(TeamMemberDTO.class);

        // Assert
        assertThat(newMember.getUserId(), equalTo(contact.getId()));
        assertThat(newMember.getName(), equalTo("Ferdinand Stiller"));
    }

    @Test
    @DisplayName("Invite Contact presenting already existing User to Team.")
    public void inviteAlreadyExistingUserViaContact() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser user = testUserHelper.newTestUser();
        ProjectContact contact = new ProjectContact(projectId);
        contact.setEmail(user.getUsername());
        contactRepository.save(contact);

        TeamMemberDTO body = new TeamMemberDTO();
        body.setUserId(contact.getId());

        // Act
        given()
                .contentType("application/json")
                .body(body)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .post("/api/v2/projects/{projectId}/team", projectId)
        .then()
                .log().all()
                .statusCode(201)
                .extract().as(TeamMemberDTO.class);

        // Assert
        given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()//
        .when()
                .get("/api/v2/contacts/{contactId}", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200)
                .body("replaced", is(true))
                .body("replacedById", equalTo(user.getId().toString()));
    }

    @Test
    @DisplayName("Invite Contact that is already member of Team.")
    public void inviteMemberViaContact() {
        // Arrange
        UUID projectId = UUID.randomUUID();

        DocutoolsUser user = testUserHelper.newTestUser();
        TeamMemberDTO membership = new TeamMemberDTO();
        membership.setUserId(user.getId());
        given()
                .contentType("application/json")
                .body(membership)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
                .when()
                .post("/api/v2/projects/{projectId}/team", projectId)
                .then()
                .log().all()
                .statusCode(201)
                .extract().as(TeamMemberDTO.class);

        ProjectContact contact = new ProjectContact(projectId);
        contact.setEmail(user.getUsername());
        contactRepository.save(contact);

        TeamMemberDTO body = new TeamMemberDTO();
        body.setUserId(contact.getId());

        // Act
        given()
                .contentType("application/json")
                .body(body)
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()
        .when()
                .post("/api/v2/projects/{projectId}/team", projectId)
        .then()
                .log().all()
                .statusCode(201)
                .extract().as(TeamMemberDTO.class);

        // Assert
        given()
                .accept("application/json")
                .auth().oauth2(token)
                .log().all()//
        .when()
                .get("/api/v2/contacts/{contactId}", contact.getId()) //
        .then()
                .log().all()
                .statusCode(200)
                .body("replaced", is(true))
                .body("replacedById", equalTo(user.getId().toString()));
    }

}
