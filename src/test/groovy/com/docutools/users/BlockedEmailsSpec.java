package com.docutools.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BlockedEmailsSpec {

    @Test
    public void loadBlockedEmails() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        // Act
        new BlockedEmails(objectMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"roland.tiefenbacher@strabag.com", "johannes.otter@zueblin.nl", "peter.raber@fk-systembau.de", "ferl@bitunova.eu"})
    public void checkBlockedEmails(String message) throws Exception {
        // Arrange
        BlockedEmails blockedEmails = new BlockedEmails(new ObjectMapper());
        // Act
        boolean blocked = blockedEmails.isBlocked(message);
        // Assert
        Assertions.assertTrue(blocked);
    }

    @ParameterizedTest
    @ValueSource(strings = {"roland.tiefenbacher@gmail.com", "johannes.otter@outlook.test", "peter.raber@rayray.co", "ferl@kao.de"})
    public void checkUnblockedEmails(String message) throws Exception {
        // Arrange
        BlockedEmails blockedEmails = new BlockedEmails(new ObjectMapper());
        // Act
        boolean blocked = blockedEmails.isBlocked(message);
        // Assert
        Assertions.assertFalse(blocked);
    }

}
