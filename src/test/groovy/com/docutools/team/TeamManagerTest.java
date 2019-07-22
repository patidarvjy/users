package com.docutools.team;

import com.docutools.roles.DefaultRoles;
import com.docutools.roles.Role;
import com.docutools.roles.RoleRepo;
import com.docutools.roles.RoleType;
import com.docutools.services.projects.ProjectApiClient;
import com.docutools.services.projects.resources.Project;
import com.docutools.test.TestUserHelper;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.docutools.users.OrganisationRepo;
import com.docutools.users.SessionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
@DisplayName("TeamManagerTest")
public class TeamManagerTest {

    private static final int TEST_PROJECT_NUMBER = 500;

    @Autowired
    private TeamManager teamManager;

    @Autowired
    private TestUserHelper testUserHelper;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private TeamMembershipRepo teamMembershipRepo;

    @Autowired
    private OrganisationRepo organisationRepo;

    @MockBean
    private SessionManager sessionManager;

    @MockBean
    private ProjectApiClient projectApi;

    @Test
    public void getProjectIdListFromUserIdInExpectedTime() {
        //GIVEN
        DocutoolsUser user = testUserHelper.newTestUser();
        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user);
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);

        IntStream.range(0, TEST_PROJECT_NUMBER).forEach(i -> {
            UUID projectId = UUID.randomUUID();
            TeamMembership membership = new TeamMembership(user, projectId, MembershipState.Active);
            roleRepository.saveAll(roles);
            Role viewerRole = roleRepository.save(viewer);
            //membership.setRoles(new HashSet<>(roles));
            membership.setRole(viewerRole);
            teamMembershipRepo.save(membership);
        });

        //WHEN
        long t1 = System.currentTimeMillis();
        List<UUID> projectIdListFromSqlQuery = teamManager.getProjectIdListFromUserId(user.getId());
        long t2 = System.currentTimeMillis();
        List<UUID> projectIdListFromMemberships = teamManager.listAllMemberships(user.getId()).stream().map(TeamMemberDTO::getProjectId).collect(Collectors.toList());
        long t3 = System.currentTimeMillis();

        //THEN
        assertEquals(projectIdListFromSqlQuery.size(), TEST_PROJECT_NUMBER);
        assertEquals(projectIdListFromMemberships.size(), TEST_PROJECT_NUMBER);
        assertTrue((t2 - t1) < 100);
        assertTrue((t2 - t1) < (t3 - t2));
    }

    @Test
    public void getProjectIdListPagedFromUserId() {
        //GIVEN
        DocutoolsUser user = testUserHelper.newTestUser();
        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user);
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);

        Project project = new Project();
        project.setId(UUID.randomUUID());

        TeamMembership membership = new TeamMembership(user, project.getId(), MembershipState.Active);
        roleRepository.saveAll(roles);
        Role viewerRole = roleRepository.save(viewer);
        //membership.setRoles(new HashSet<>(roles));
        membership.setRole(viewerRole);
        teamMembershipRepo.save(membership);

        Mockito.when(projectApi.getAllProjectsInCurrentOrganisation(0, 10)).thenReturn(Collections.singletonList(project));

        //WHEN
        List<TeamMemberDTO> teamMemberDTOS = teamManager.listMemberships(user, 0, 10, null, null, null);
        //THEN
        assertEquals(teamMemberDTOS.size(), 1);
    }

    @Test
    public void getUserProjectsWithRemovedMemberships() {
        int pageSize = 50;
        int totalMemberships = 100;
        //GIVEN
        DocutoolsUser user = testUserHelper.newTestUser();
        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user);
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);

        IntStream.range(0, totalMemberships).forEach(i -> {
            UUID projectId = UUID.randomUUID();
            TeamMembership membership = new TeamMembership(user, projectId, MembershipState.Removed);
            roleRepository.saveAll(roles);
            Role viewerRole = roleRepository.save(viewer);
            membership.setRole(viewerRole);
            membership.setLastModifiedBy(user.getId());
            membership.setLastModified(ZonedDateTime.now());
            teamMembershipRepo.save(membership);
        });
        //WHEN
        List<UUID> leftProjectIdListFromUserId = teamManager.getLeftProjectIdListFromUserId(user.getId(), 0, pageSize, Instant.now().minusSeconds(1));

        //THEN
        assertFalse(leftProjectIdListFromUserId.isEmpty());
        assertEquals(pageSize, leftProjectIdListFromUserId.size());
    }

    @Test
    public void getTeamMemberByProjectAndUserId() {
        //GIVEN
        DocutoolsUser user = testUserHelper.newTestUser();
        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user);
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);

        UUID projectId = UUID.randomUUID();
        TeamMembership membership = new TeamMembership(user, projectId, MembershipState.Active);
        roleRepository.saveAll(roles);
        Role viewerRole = roleRepository.save(viewer);
        membership.setRole(viewerRole);
        membership.setLastModifiedBy(user.getId());
        membership.setLastModified(ZonedDateTime.now());
        teamMembershipRepo.save(membership);

        Project project = new Project();
        project.setId(projectId);
        project.setOrganisationId(user.getOrganisation().getId());

        Mockito.when(projectApi.getProject(projectId)).thenReturn(project);

        //WHEN
        TeamMemberDTO teamMemberDTO = teamManager.getTeamMembership(projectId, user.getId());

        //THEN
        assert teamMemberDTO != null;
        assertEquals(MembershipState.Active, teamMemberDTO.getState());
    }

    @Test
    public void searchTeamMembers() {
        //GIVEN
        int page = 0;
        int size = 10;

        Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisationRepo.save(organisation);

        DocutoolsUser user = testUserHelper.newTestUser(organisation, true, true, true);
        user.setJobTitle("Test job title");
        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);
        List<Role> roles = Arrays.asList(powerUser, viewer);

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setOrganisationId(organisation.getId());

        TeamMembership membership = new TeamMembership(user, project.getId(), MembershipState.Active);
        roleRepository.saveAll(roles);
        Role viewerRole = roleRepository.save(viewer);
        membership.setRole(viewerRole);
        membership.setLastModifiedBy(user.getId());
        membership.setLastModified(ZonedDateTime.now());
        teamMembershipRepo.save(membership);

        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user);
        Mockito.when(projectApi.getProject(project.getId())).thenReturn(project);

        //WHEN
        List<TeamMembership> teamMemberships = teamMembershipRepo.searchTeamMembers(project.getId(), "Manager", PageRequest.of(page, size));

        //THEN
        assert !CollectionUtils.isEmpty(teamMemberships);
        assert teamMemberships.size() == 1;
    }

    @Test
    public void checkRepository() {
        Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisationRepo.save(organisation);

        DocutoolsUser user1 = testUserHelper.newTestUser(organisation, true, true, true);
        user1.setJobTitle("Test job title");
        Role powerUser1 = new Role("Power User", Collections.emptySet(), user1.getOrganisation(), user1, RoleType.PowerUser, true);
        Role viewer1 = new Role("Viewer", Collections.emptySet(), user1.getOrganisation(), user1, RoleType.Viewer, true);
        List<Role> roles1 = Arrays.asList(powerUser1, viewer1);

        DocutoolsUser user2 = testUserHelper.newTestUser(organisation, true, true, true);
        user2.setJobTitle("Test job title");
        Role powerUser2 = new Role("Power User", Collections.emptySet(), user2.getOrganisation(), user2, RoleType.PowerUser, true);
        Role viewer2 = new Role("Viewer", Collections.emptySet(), user2.getOrganisation(), user2, RoleType.Viewer, true);
        List<Role> roles2 = Arrays.asList(powerUser2, viewer2);

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setOrganisationId(organisation.getId());

        TeamMembership membership1 = new TeamMembership(user1, project.getId(), MembershipState.Active);
        roleRepository.saveAll(roles1);
        Role viewerRole1 = roleRepository.save(viewer1);
        membership1.setRole(viewerRole1);
        membership1.setLastModifiedBy(user1.getId());
        membership1.setLastModified(ZonedDateTime.now());
        teamMembershipRepo.save(membership1);

        TeamMembership membership2 = new TeamMembership(user2, project.getId(), MembershipState.Active);
        roleRepository.saveAll(roles2);
        Role viewerRole2 = roleRepository.save(viewer2);
        membership2.setRole(viewerRole2);
        membership2.setLastModifiedBy(user2.getId());
        membership2.setLastModified(ZonedDateTime.now());
        teamMembershipRepo.save(membership2);

        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user1);
        Mockito.when(projectApi.getProject(project.getId())).thenReturn(project);

        //WHEN
        List<TeamMembership> members = teamMembershipRepo.findMembers(Arrays.asList(user1.getId(), user2.getId()), project.getId());

        assert members.size() == 2;
    }

    @Test
    public void getCurrentUserRoleMap(){
        //GIVEN
        DocutoolsUser user = testUserHelper.newTestUser();
        Mockito.when(sessionManager.getCurrentUser()).thenReturn(user);

        Role powerUser = new Role("Power User", Collections.emptySet(), user.getOrganisation(), user, RoleType.PowerUser, true);
        Role viewer = new Role("Viewer", Collections.emptySet(), user.getOrganisation(), user, RoleType.Viewer, true);

        UUID projectId = UUID.randomUUID();
        TeamMembership membership = new TeamMembership(user, projectId, MembershipState.Active);
        membership.setRole(viewer);
        teamMembershipRepo.save(membership);

        UUID projectId2 = UUID.randomUUID();
        TeamMembership membership2 = new TeamMembership(user, projectId2, MembershipState.Active);
        membership2.setRole(powerUser);
        teamMembershipRepo.save(membership2);

        //WHEN
        List<RoleMapDTO> roleMaps = teamManager.getRoleMapForCurrentUser();

        //THEN
        assert roleMaps != null;
        assertEquals(2, roleMaps.size());
    }
}
