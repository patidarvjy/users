package com.docutools.roles;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static com.docutools.exceptions.ExceptionHelper.*;

public class DefaultRoles {

    /**
     * Singleton instance constant.
     */
    public static final DefaultRoles instance = new DefaultRoles();

    private static final String DEFAULT_ROLE_DATA_RESOURCE_PATH = "/DefaultRoleData.json";

    private List<DefaultRoleData> roleData;
    private List<RoleType> activeRoleTypes;

    private DefaultRoles(){
        Resource resource = new ClassPathResource(DEFAULT_ROLE_DATA_RESOURCE_PATH);
        try (InputStream in = resource.getInputStream()){
            roleData = Jackson2ObjectMapperBuilder.json().build().readValue(in, new TypeReference<List<DefaultRoleData>>(){});
        } catch (IOException e) {
            throw newInternalServerError("Can not read default role data from file!");
        }
    }

    public List<DefaultRoleData> getRoleData(){
        return roleData;
    }

    public List<RoleType> getActiveDefaultRoleTypes(){
        if(activeRoleTypes == null){
            activeRoleTypes = roleData.stream().filter(DefaultRoleData::isActive).map(DefaultRoleData::getRoleType).collect(Collectors.toList());
        }
        return activeRoleTypes;
    }

    /**
     * Looks up the privileges of the {@link DefaultRoleData} for the given type and returns it as List.
     *
     * @param type type of the {@link DefaultRoleData}
     * @return list of {@link Privilege}s
     */
    public Optional<Set<Privilege>> getPrivilegesForRoleType(RoleType type) {
        return roleData.stream()
                .filter(role -> role.getRoleType() == type)
                .map(DefaultRoleData::getPrivileges)
                .findAny();
    }

}
