package com.docutools.login;

import com.docutools.users.DocutoolsUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class LoginCountUnitTests {

    @Test
    public void checkTracking() {
        DocutoolsUser user = new DocutoolsUser();
        user.setId(UUID.randomUUID());
        LoginCount count = new LoginCount(user);
        count.trackLogin();
        count.trackLogin();
        Assertions.assertEquals(3, count.trackLogin());
    }

}
