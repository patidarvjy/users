package com.docutools.mfa.sms;

import com.docutools.mfa.MFAService;
import com.docutools.mfa.sms.resources.VerifyCodeDTO;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.SessionManager;
import com.docutools.users.UserRepo;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.docutools.users.UserManager.getCurrentActor;

/**
 * Provides SMS Based Multi Factor Authentication operations
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SmsBasedMFAController {

    private static final Logger log = LoggerFactory.getLogger(SmsBasedMFAController.class);

    @Autowired
    private MFAService service;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private UserRepo userRepo;


    @ApiOperation("Actives receive 2FA verification code by SMS")
    @PostMapping("/mfa-api/v1/sms")
    public void activate() {
        log.info("POST /mfa-api/v1/mfa by {}", getCurrentActor());

        final DocutoolsUser user = sessionManager.getCurrentUser();
        service.active(user);
    }

    @ApiOperation("Validates 2FA verification code sent by SMS")
    @PostMapping("/mfa-api/v1/sms/verification")
    public void verify(@RequestBody VerifyCodeDTO verifyCode) {
        log.info("POST /mfa-api/v1/verification by {}", getCurrentActor());

        final DocutoolsUser user = sessionManager.getCurrentUser();
        service.verify(user, verifyCode.getCode());
    }

    @ApiOperation("Disables receive 2FA verification code by SMS")
    @DeleteMapping("/mfa-api/v1/sms")
    public void deactivate() {
        log.info("DELETE /mfa-api/v1/mfa by {}", getCurrentActor());

        final DocutoolsUser user = sessionManager.getCurrentUser();
        service.deactivate(user);
    }

    @ApiOperation("Requests a 2FA verification code sending by SMS to user phone")
    @GetMapping("/mfa-api/v1/sms")
    @PreAuthorize("isAnonymous()")
    public void requestVerificationCode(@RequestParam String email) {
        log.info("GET /mfa-api/v1/sms by {}", getCurrentActor());

        userRepo.findByUsernameIgnoreCase(email)
                .filter(docutoolsUser -> docutoolsUser.getSettings().getSmsFactorAuthEnabled())
                .ifPresent((user) -> service.sendVerificationCodeBySMS(user));
    }

}
