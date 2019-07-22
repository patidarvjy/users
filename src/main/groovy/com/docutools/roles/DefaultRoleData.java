package com.docutools.roles;

import com.docutools.users.DocutoolsUser;
import com.docutools.users.Organisation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

public class DefaultRoleData {

    private static String DEFAULT_LANGUAGE = "en";

    private Map<String, String> languageNameMap;
    private Set<Privilege> privileges;
    private RoleType roleType;
    private boolean active = true;

    @JsonCreator
    public DefaultRoleData(@JsonProperty(value = "languageNameMap", required = true) Map<String, String> languageNameMap,
                           @JsonProperty(value = "privileges", required = true) Set<Privilege> privileges,
                           @JsonProperty(value = "roleType", required = true) RoleType roleType,
                           @JsonProperty(value = "active") Boolean active) {
        Assert.isTrue(languageNameMap.containsKey(DEFAULT_LANGUAGE), "Rolename for Default Language is required!");
        this.languageNameMap = languageNameMap;
        this.privileges = privileges;
        this.roleType = roleType;
        if(active != null) {
            this.active = active;
        }
    }

    public Map<String, String> getLanguageNameMap() {
        return languageNameMap;
    }

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getLanguageName(String language){
        if(languageNameMap.containsKey(language)){
            return languageNameMap.get(language);
        }
        return languageNameMap.get(DEFAULT_LANGUAGE);
    }

    public Role createRole(Organisation organisation){
        DocutoolsUser currentUser = organisation.getOwner();
        String language = currentUser.getSettings().getLanguage();
        return new Role(getLanguageName(language), privileges, organisation, currentUser, roleType, active);
    }

    public boolean isActive(){
        return active;
    }

    @Override
    public String toString() {
        return String.format("DefaultRole {name: %s, active: %b}",
                StringUtils.quote(languageNameMap.getOrDefault(DEFAULT_LANGUAGE, "")), active);
    }
}
