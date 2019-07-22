package com.docutools.password;

import com.docutools.config.security.PasswordEncoder;
import com.docutools.test.AlternativeFacts;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.values.PersonName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.io.IOException;

@Tag("unit")
public class PasswordPolicyUnitTest {

    private PasswordPolicy passwordPolicy;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PasswordPolicies policies = new PasswordPolicies(objectMapper);
        passwordPolicy = policies.get("strong");
        Pbkdf2PasswordEncoder pbkdf2PasswordEncoder = new Pbkdf2PasswordEncoder("yXD6Z9sV", 64_000, 128);
        passwordEncoder = new PasswordEncoder(pbkdf2PasswordEncoder, 0, 0, 0, 0, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678_M", "12345678_m", "12345678Mm", "MMMMmmmm_", "_Mm1", "oldPassword_1", "Max123___", "Mustermann123____"})
    public void testInvalidPasswords(String password) {
        // Arrange
        DocutoolsUser user = new DocutoolsUser();
        PersonName name = new PersonName();
        name.setFirstName("Max");
        name.setLastName("Mustermann");
        user.setName(name);
        user.setPassword(passwordEncoder.hashPassword("oldPassword_1"));
        // Act
        boolean valid = passwordPolicy.validate(password, passwordEncoder, user);
        // Assert
        Assertions.assertFalse(valid, String.format("Password '%s' shouldn't be valid!", password));
    }

    @Test
    public void testValidPassword() {
        // Arrange
        DocutoolsUser user = new DocutoolsUser();
        PersonName name = new PersonName();
        name.setFirstName(AlternativeFacts.firstName());
        name.setLastName(AlternativeFacts.lastName());
        user.setName(name);
        user.setPassword(passwordEncoder.hashPassword("oldPassword_1"));
        // Act
        String newPassword = "newPassword_1";
        boolean valid = passwordPolicy.validate(newPassword, passwordEncoder, user);
        // Assert
        Assertions.assertTrue(valid, String.format("Password '%s' should be valid!", newPassword));
    }

}
