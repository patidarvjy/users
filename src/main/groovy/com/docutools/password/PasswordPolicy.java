package com.docutools.password;

import com.docutools.config.security.PasswordEncoder;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.values.Password;
import com.docutools.users.values.PersonName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A policy for password strength.
 *
 * @author amp
 */
public class PasswordPolicy implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(PasswordPolicy.class);

    private static final Pattern LOWER_CASE_CHARACTERS = Pattern.compile("[a-z]");
    private static final Pattern UPPER_CASE_CHARACTERS = Pattern.compile("[A-Z]");
    private static final Pattern NUMBER_CHARACTERS = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[^A-Za-z0-9]+");

    private int minimumLength;
    private int maximumLength;
    private Set<CharacterClass> requiredCharacterClasses;
    private int daysToLife;
    private boolean blockReuse;
    private boolean blockNames;

    @JsonCreator
    public PasswordPolicy(@JsonProperty(value = "min") int minimumLength,
                          @JsonProperty(value = "max") int maximumLength,
                          @JsonProperty("classes") Set<CharacterClass> requiredCharacterClasses,
                          @JsonProperty(value = "dtl") int daysToLife,
                          @JsonProperty(value = "blockReuse") boolean blockReuse,
                          @JsonProperty(value = "blockNames") boolean blockNames) {
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
        this.requiredCharacterClasses = requiredCharacterClasses != null? requiredCharacterClasses : Collections.emptySet();
        this.daysToLife = daysToLife;
        this.blockReuse = blockReuse;
        this.blockNames = blockNames;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public Set<CharacterClass> getRequiredCharacterClasses() {
        return requiredCharacterClasses;
    }

    public int getDaysToLife() {
        return daysToLife;
    }

    public boolean getBlockReuse() {
        return blockReuse;
    }

    public boolean getBlockNames() {
        return blockNames;
    }

    /**
     * Validates the given clear text password against the password policy.
     *
     * @param clearTextPassword the clear text password.
     * @param passwordEncoder the {@link PasswordEncoder} to check against {@link DocutoolsUser#getPassword()}.
     * @param user the {@link DocutoolsUser} to check for email and names.
     * @return {@code true} when valid.
     */
    public boolean validate(String clearTextPassword, PasswordEncoder passwordEncoder, DocutoolsUser user) {
        if(clearTextPassword == null || clearTextPassword.isEmpty())
            return false;
        if(minimumLength > 0 && clearTextPassword.length() < minimumLength)
            return false;
        if(maximumLength > 0 && maximumLength > minimumLength && clearTextPassword.length() > maximumLength)
            return false;
        for(CharacterClass characterClass : requiredCharacterClasses) {
            switch (characterClass) {
                case LOWER_CASE:
                    if(!LOWER_CASE_CHARACTERS.matcher(clearTextPassword).find())
                        return false;
                    break;
                case UPPER_CASE:
                    if(!UPPER_CASE_CHARACTERS.matcher(clearTextPassword).find())
                        return false;
                    break;
                case NUMBERS:
                    if(!NUMBER_CHARACTERS.matcher(clearTextPassword).find())
                        return false;
                    break;
                case SPECIAL:
                    if(!SPECIAL_CHARACTERS.matcher(clearTextPassword).find())
                        return false;
                    break;
                default:
                    log.warn("Unknown Character Class: {}", characterClass);
                    break;
            }
        }

        if(blockNames && containsNamesOrEmail(clearTextPassword, user)) {
            return false;
        }

        if(blockReuse && user.getPassword() != null) {
            return user.getPasswordLog().stream()
                    .noneMatch(hash -> passwordEncoder.checkPassword(new Password(hash), clearTextPassword));
        }

        return true;
    }

    /**
     * Tests if the given {@link Password} is expired based on the policies {@link this#daysToLife} value.
     *
     * @param password the {@link Password}
     * @return {@code true} when expired.
     */
    public boolean requiresPasswordChange(Password password) {
        return daysToLife > 0 && password != null && password.getLastChanged() != null
                && Period.between(password.getLastChanged().toLocalDate(), LocalDate.now()).getDays() > daysToLife;
    }

    private boolean containsNamesOrEmail(String clearTextPassword, DocutoolsUser user) {
        PersonName name = user.getName();
        return containsIgnoreCase(clearTextPassword, user.getUsername()) ||
                (name != null && !isNullOrEmpty(name.getFirstName()) && containsIgnoreCase(clearTextPassword, name.getFirstName())) ||
                (name != null && !isNullOrEmpty(name.getLastName()) && containsIgnoreCase(clearTextPassword, name.getLastName()));
    }

    private static boolean containsIgnoreCase(String str, String searchStr)     {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

    private static boolean isNullOrEmpty(String test) {
        return test == null || test.isEmpty();
    }
}
