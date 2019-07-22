package com.docutools.config.security;

import com.docutools.users.values.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@Tag("unit")
@DisplayName("Encoding Passwords")
public class PasswordEncoding {

    private PasswordEncoder encoder;

    @BeforeEach
    public void setup() {
        Pbkdf2PasswordEncoder pbkdf2PasswordEncoder = new Pbkdf2PasswordEncoder("yXD6Z9sV", 64_000, 128);
        this.encoder = new PasswordEncoder(pbkdf2PasswordEncoder, 0, 0, 0, 0, 0);
    }

    @ParameterizedTest(name = "run #{index} with [{arguments}]")
    @ValueSource(strings = {"secret"})
    public void encodePasswords(String plainText) {
        Password password = encoder.hashPassword(plainText);
        assertThat(encoder.checkPassword(password, plainText), is(true));
        System.out.printf("%s => %s%n", plainText, password.getHash());
    }

}
