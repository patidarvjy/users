package com.docutools.team;

import com.docutools.roles.Role;
import com.docutools.roles.RoleType;

import java.io.Serializable;
import java.util.UUID;

public class RoleMapDTO implements Serializable {

    private UUID projectId;
    private RoleType role;
    // For iOS backward compability
    private String name;

    public RoleMapDTO(TeamMembership membership) {
        this.projectId = membership.getProjectId();
        this.role = membership.getRole() != null? membership.getRole().getRoleType()
                : membership.getRoles().stream().findFirst().map(Role::getRoleType).orElse(RoleType.SubContractor);
        this.name = membership.getRole() != null ? membership.getRole().getName()
                : membership.getRoles().stream().findFirst().map(Role::getName).orElse(RoleType.SubContractor.toString());
    }

    public UUID getProjectId() {
        return projectId;
    }

    public RoleType getRole() {
        return role;
    }

    public String getName() {
        return name;
    }
}
