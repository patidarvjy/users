package com.docutools.login;

import com.docutools.test.DocutoolsTestUser;
import com.docutools.test.TestUserHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@ExtendWith(SpringExtension.class)
public class LoginTrackerIntegrationTest {

    @Autowired
    private TestUserHelper testUserHelper;
    @LocalServerPort
    private int localServerPort;
    @Autowired
    private LoginCountRepository countRepository;

    @Test
    public void trackLoginsForUser() {
        DocutoolsTestUser user = testUserHelper.newTestUser();
        testUserHelper.login(user, localServerPort);
        LoginCount count = countRepository.getOne(user.getId());
        Assertions.assertNotNull(count);
        Assertions.assertEquals(2, count.trackLogin());
    }

}
