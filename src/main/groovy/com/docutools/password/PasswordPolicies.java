package com.docutools.password;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring bean providing different {@link PasswordPolicy} configurations.
 *
 * Tries to deserialize the policies {@code weak} and {@code strong} from the resource files
 * {@code defintions/strong/weak-policy.json}.
 *
 * @author amp
 */
@Component
public class PasswordPolicies {

    private Map<String,PasswordPolicy> policyMap;

    @Autowired
    public PasswordPolicies(ObjectMapper objectMapper) throws IOException {
        this.policyMap = new HashMap<>();
        loadPolicy(objectMapper, "strong");
        loadPolicy(objectMapper, "weak");
    }

    /**
     * Returns the {@link PasswordPolicy} with the given name or the {@link this#getDefault()}.
     *
     * @param name name of {@link PasswordPolicy}
     * @return the {@link PasswordPolicy}
     */
    public PasswordPolicy get(String name) {
        return policyMap.getOrDefault(name, getDefault());
    }

    /**
     * Returns the weak {@link PasswordPolicy}.
     *
     * @return default {@link PasswordPolicy}.
     */
    public PasswordPolicy getDefault() {
        return policyMap.get("weak");
    }

    private void loadPolicy(ObjectMapper objectMapper, String policyName) throws IOException {
        String policyResource = String.format("definitions/password/%s-policy.json", policyName);
        ClassPathResource resource = new ClassPathResource(policyResource);
        try(InputStream in = resource.getInputStream()) {
            policyMap.put(policyName, objectMapper.readValue(in, PasswordPolicy.class));
        }
    }
}
