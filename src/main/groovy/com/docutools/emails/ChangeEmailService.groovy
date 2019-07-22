package com.docutools.emails

import com.docutools.exceptions.ExceptionHelper
import com.docutools.exceptions.ErrorCodes
import com.docutools.users.ChangeEmailRequestRepo
import com.docutools.users.UserRepo
import com.docutools.users.values.ChangeEmailRequest
import com.docutools.config.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.LocalDate

import static com.docutools.exceptions.ErrorCodes.*
import static com.docutools.exceptions.ExceptionHelper.*

/**
 * Handles the process of changing an users email address by requesting his password and sending a verification email.
 */
@Service
class ChangeEmailService {

    @Autowired
    UserRepo userRepo;
    @Autowired
    PasswordEncoder pwEncoder;
    @Autowired
    ChangeEmailRequestRepo reqRepo;
    @Autowired
    private MailServer mailServer

    public void changeEmailAddress(String username, String password, String newEmail) {
        def user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow({ throw newBadRequestError(USER_NOT_FOUND)})
        if(!(pwEncoder.checkPassword(user.getPassword(), password))) {
            throw newBadRequestError(INCORRECT_PASSWORD);
        }
        if(userRepo.findByUsernameIgnoreCase(newEmail).present) {
            throw newBadRequestError(EMAIL_IN_USE, newEmail);
        }
        ChangeEmailRequest request = reqRepo.save(new ChangeEmailRequest(user: user, newEmail: newEmail))
        mailServer.sendEmail(EmailTemplateType.ChangeEmail, [changeDate: LocalDate.now(), fromEmail: user.username, toEmail: request.newEmail, verificationToken: request.verificationToken], user)
    }

    public void verifyNewEmailAddress(String token) {
        def request = reqRepo.findByVerificationToken(token)
                .orElseThrow({ newBadRequestError(RESOURCE_NOT_FOUND, "Unknown Token")});

        if(request.verified){
            throw newBadRequestError(EMAIL_ALREADY_VERIFIED, request.newEmail);
        }

        if(userRepo.findByUsernameIgnoreCase(request.newEmail).isPresent()) {
            throw newBadRequestError(EMAIL_IN_USE, request.newEmail);
        }

        def user = request.getUser()
        user.setUsername(request.newEmail)
        user.setEmail(request.newEmail)
        userRepo.save(user)

        request.setVerified(true)
        reqRepo.save(request)
    }

}
