package com.docutools.login;

import com.docutools.users.DocutoolsUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Tracks the total number of logins a {@link DocutoolsUser} has. The number can be queried via the {@link LoginCount} entity.
 *
 * @author amp
 */
@Service
public class LoginTracker {

    private static final Logger log = LoggerFactory.getLogger(LoginTracker.class);

    @Autowired
    private LoginCountRepository repository;
    @Autowired
    private LoginLogRepository loginLogRepository;

    /**
     * Tracks a new login for the given {@link DocutoolsUser} asynchronously.
     *
     * @param user the {@link DocutoolsUser}
     */
    public void trackLogin(DocutoolsUser user) {
        if(user == null) {
            log.warn("Could not track login: user is null.");
        }
        CompletableFuture.runAsync(() -> doTrackLogin(user));
    }

    private void doTrackLogin(DocutoolsUser user) {
        try {
            LoginCount count = repository.findById(user.getId())
                    .orElseGet(() -> new LoginCount(user));
            int newTotal = count.trackLogin();
            repository.save(count);
            log.debug("Tracked {} logins for user {}.", newTotal, user.getId());
        } catch (Exception e) {
            log.warn("Could not track login.", e);
        }
        try {
            LoginLog newLog = new LoginLog(user);
            loginLogRepository.save(newLog);
            log.debug("Logged {} login.", newLog);
        } catch (Exception e) {
            log.warn("Could not track login.", e);
        }
    }

}
