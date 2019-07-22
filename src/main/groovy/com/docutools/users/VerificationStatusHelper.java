package com.docutools.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.time.LocalDateTime.now;

@Component
public class VerificationStatusHelper {

    @Autowired
    UserRepo userRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateExpiryTime(DocutoolsUser user){
        user.getVerificationStatus().setExpiryTime(now().plusDays(7));
        userRepo.save(user);
    }
}
