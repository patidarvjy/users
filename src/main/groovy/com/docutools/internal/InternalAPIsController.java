package com.docutools.internal;

import com.docutools.roles.PermissionManager;
import com.docutools.roles.Privilege;
import com.docutools.roles.PrivilegeCheckDTO;
import com.docutools.team.TeamMembership;
import com.docutools.team.TeamMembershipRepo;
import com.docutools.users.UserRepo;
import com.docutools.users.resources.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.docutools.exceptions.ExceptionHelper.newResourceNotFoundError;
import static com.docutools.exceptions.ExceptionHelper.newUnauthorizedError;

@RestController
public class InternalAPIsController {

    private static final Logger log = LoggerFactory.getLogger(InternalAPIsController.class);

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TeamMembershipRepo teamMembershipRepo;
    @Autowired
    private PermissionManager permissionManager;


    @Value("${docutools.internal.apiKey:b7abbc58-ef02-4a1f-a85a-e47c4e25b2ce}")
    private String apiKey;

    @GetMapping(path = "/api/internal/v2/users/{userId}")
    public UserDTO getUserForId(@PathVariable UUID userId,
                                @RequestHeader("X-AUTH-TOKEN") String apiKey) {
        if (!this.apiKey.equals(apiKey)) {
            throw newUnauthorizedError();
        }
        log.debug("GET /api/internal/v2/users/{}", userId);

        return userRepo.findById(userId)
            .map(UserDTO::new)
            .orElseThrow(()-> newResourceNotFoundError("User", userId));
    }

    @GetMapping(path = "/api/internal/v2/team/{projectId}")
    public List<UserDTO> getTeamForProject(@PathVariable UUID projectId,
                                           @RequestHeader("X-AUTH-TOKEN") String apiKey) {
        if (!this.apiKey.equals(apiKey)) {
            throw newUnauthorizedError();
        }
        log.debug("GET /api/internal/v2/team/{}", projectId);
        return teamMembershipRepo.findTeam(projectId)
            .stream()
            .map(TeamMembership::getUser)
            .map(UserDTO::new)
            .collect(Collectors.toList());
    }


    @GetMapping(path = "/api/internal/v2/checkPrivilege")
    public PrivilegeCheckDTO checkPrivilege(@RequestParam UUID projectId, @RequestParam UUID userId,
                                            @RequestParam(required = false, defaultValue = "false") boolean any,
                                            @RequestParam List<Privilege> privileges,
                                            @RequestHeader("X-AUTH-TOKEN") String apiKey) {
        if (!this.apiKey.equals(apiKey)) {
            throw newUnauthorizedError();
        }
        log.debug("GET /api/internal/v2/checkPrivilege?projectId={}&privileges={}&any={} by {}", projectId, privileges, any, userId);
        if (privileges == null || privileges.isEmpty()) {
            privileges = Collections.emptyList();
        }
        return permissionManager.checkPrivilegeInternal(userId, projectId, privileges, any);
    }


    @GetMapping(path = "/api/internal/v2/checkMember")
    public boolean isMember(@RequestParam UUID projectId, @RequestParam UUID userId,
                            @RequestHeader("X-AUTH-TOKEN") String apiKey) {
        if (!this.apiKey.equals(apiKey)) {
            throw newUnauthorizedError();
        }
        log.debug("GET /api/internal/v2/checkMember?projectId={} by {}", projectId, userId);

        return permissionManager.isMembershipExist(userId, projectId);
    }
}
