package com.docutools.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * List of Email Domains with whom users are not allowed to register over the form (customers have SAML Integrations).
 *
 * @author amp
 */
@Component
public class BlockedEmails {

    private final Set<String> set;

    @Autowired
    public BlockedEmails(ObjectMapper objectMapper) throws IOException {
        try(InputStream in = new ClassPathResource("misc/locked-email-domains.json").getInputStream()) {
            String[] array = objectMapper.readerFor(String[].class)
                    .readValue(in);
            if(array == null) {
                throw new BeanCreationException("Read NULL value from misc/locked-email-domains.json");
            }
            set = Arrays.stream(array).collect(Collectors.toSet());
        }
    }

    /**
     * Tests if the given Emails domain is blocked.
     *
     * @param email the test email
     * @return {@code true} when blocked.
     * @throws IllegalArgumentException when the email is {@code null}.
     */
    public boolean isBlocked(String email) {
        Assert.notNull(email, "email is required - must not be NULL!");
        return set.stream()
                .anyMatch(email::endsWith);
    }

}
