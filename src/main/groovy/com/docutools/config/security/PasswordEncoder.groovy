package com.docutools.config.security

import com.docutools.exceptions.ExceptionHelper
import com.docutools.exceptions.ErrorCodes
import com.docutools.users.values.Password
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.Assert

import static java.time.LocalDateTime.now

/**
 * Service class for encoding and validating user passwords.
 */
@Service
class PasswordEncoder {

    private Pbkdf2PasswordEncoder encoder

    // Password Strength Configurations
    private int minSize
    private int minUpperCaseLetters
    private int minLowerCaseLetters
    private int minSymbols
    private int minNumbers

    @Autowired
    def PasswordEncoder(Pbkdf2PasswordEncoder pbkdf2PasswordEncoder,
                        @Value('${docutools.security.passwords.strength.size:8}') int minSize,
                        @Value('${docutools.security.passwords.strength.upperCase:0}') int minUpperCaseLetters,
                        @Value('${docutools.security.passwords.strength.lowerCase:0}') int minLowerCaseLetters,
                        @Value('${docutools.security.passwords.strength.symbols:0}') int minSymbols,
                        @Value('${docutools.security.passwords.strength.numbers:0}') int minNumbers) {
        this.encoder = pbkdf2PasswordEncoder

        this.minSize = minSize
        this.minUpperCaseLetters = minUpperCaseLetters
        this.minLowerCaseLetters = minLowerCaseLetters
        this.minSymbols = minSymbols
        this.minNumbers = minNumbers
    }

    /**
     * Checks if the provided clear text matches the encoded password.
     *
     * @param password encoded password
     * @param clearText clear text
     * @return true iff clear text matches
     * @throws IllegalArgumentException when password or clearText is {@code null}.
     */
    def boolean checkPassword(Password password, String clearText) {
        Assert.notNull(password)
        Assert.notNull(clearText)

        encoder.matches(clearText, password.getHash())
    }

    /**
     * Encoded the password with the configured password hash function iff the provided cleartext matches the configured
     * strength standards, by calling {@link #validatePasswordStrength(String)}.
     *
     * @param clearText clear text
     * @return encoded password
     * @throws IllegalArgumentException when the clear text is {@code null}.
     * @throws com.docutools.apierrors.ApiException when the cleartext does not meet configured strength standards.
     */
    def Password hashPassword(String clearText) {
        Assert.notNull(clearText)
        validatePasswordStrength(clearText)

        String hash = encoder.encode(clearText)
        new Password(hash: hash, lastChanged: now(), hashVersion: Password.Pbkdf2_HASH_VERSION)
    }

    /**
     * Validates the provided clearText for password strength. This can be configured in the following Spring properties:
     * <i>(All prefixed by {@code docutools.security.passwords.strength})</i>
     * <ul>
     *     <li>{@code size}, for how a long the password <b>at least</b> has to be.</li>
     *     <li>{@code upperCase/lowerCase}, for how many upper and lowercase symbols a password <b>at least</b> has to contain</li>
     *     <li>{@code numbers}, for how many digits a password has to <b>at least</b> contain.</li>
     *     <li>{@code symbols}, for how many <b>non</b> -letters and -digits a password <b>at least</b> has to contain.</li>
     * </ul>
     *
     * @param clearText clear text
     * @throws IllegalArgumentException when clearText is null
     * @throws com.docutools.apierrors.ApiException when the configured strength standards are not met.
     */
    def void validatePasswordStrength(String clearText) {
        Assert.notNull(clearText)
        if(clearText.length() >= minSize) {
            int upperCase = 0, lowerCase = 0, symbols = 0, numbers = 0
            for(char c : clearText.toCharArray()) {
                if(Character.isUpperCase(c)) upperCase++
                else if(Character.isAlphabetic(c as int) && Character.isLowerCase(c)) lowerCase++
                else if(Character.isDigit(c)) numbers++
                else symbols++
            }
            if(upperCase >= minUpperCaseLetters &&
                    lowerCase >= minLowerCaseLetters &&
                    symbols >= minSymbols &&
                    numbers >= minNumbers)
                return
        }
        String passwordLimitation = "1. Minimum length of ${minSize} symbols.\n" +
                        "2. Contain ${minUpperCaseLetters} upper case letters.\n" +
                        "3. Contain ${minLowerCaseLetters} lower case letters. \n" +
                        "4. Contain ${minNumbers} numbers. \n" +
                        "5. Contain ${minSymbols} special symbols.";
        throw ExceptionHelper.newBadRequestError(ErrorCodes.WEAK_PASSWORD, passwordLimitation)
    }

}
