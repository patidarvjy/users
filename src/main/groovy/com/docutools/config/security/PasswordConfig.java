package com.docutools.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

/**
 * Bean definitions for password encoders.
 *
 * @since 1.0
 * @author amp
 */
@Configuration
public class PasswordConfig {

    @Bean
    public Pbkdf2PasswordEncoder pbkdf2PasswordEncoder() {
        return new Pbkdf2PasswordEncoder(pbkdf2Secret, PBKDF2_ITERATIONS, PBKDF2_HASH_SIZE);
    }

    /**
     * How many iterations the PBKDF2 Algorithm shall perform. OWASP recommended 64.000 per 2012, doubling every year.
     */
    private static final int PBKDF2_ITERATIONS = 64_000; //2_048_000;
    /**
     * Size of the key, should be greater than 32 bits to decrease risk of GPU attacks.
     */
    private static final int PBKDF2_HASH_SIZE = 128;

    @Value("${docutools.security.passwords.pbkdf2.secret}")
    private String pbkdf2Secret;

}
