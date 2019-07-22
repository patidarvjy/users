package com.docutools.roles;

import java.util.Set;

public class DefaultRoleDTO {

    private Set<Privilege> privileges;
    private RoleType roleType;
    private boolean active = true;

    public DefaultRoleDTO(DefaultRoleData defaultRoleData){
        this.privileges = defaultRoleData.getPrivileges();
        this.roleType = defaultRoleData.getRoleType();
        this.active = defaultRoleData.isActive();
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Set<Privilege> privileges) {
        this.privileges = privileges;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
