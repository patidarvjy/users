package com.docutools.mfa.sms;

import com.docutools.exceptions.ErrorCodes;
import com.docutools.exceptions.ExceptionHelper;
import com.docutools.mfa.MFAService;
import com.docutools.users.DocutoolsUser;
import com.docutools.users.UserManager;
import com.docutools.users.values.UserSettings;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Implements SMS-Based Multi Factor Authentication, in other words allow to user to receive the 2 Factor verification
 * code by SMS instead use apps like Google Authenticator.
 */
@Service
public class SmsBasedMFAService implements MFAService {

    @Autowired
    private UserManager userManager;

    @Autowired
    private SMSSender smsSender;

    // SMS delays, so SMS verification code is valid for 5 minutes
    private final Clock clock = new Clock(5 * 60);

    /**
     * Request to active MFA to specific user, a SMS will be sent to user contains a verification code.
     * It's important use has a valid phone number to receive the SMS.
     *
     * @param user User will be enable MFA
     */
    @Override
    public void active(DocutoolsUser user) {
        userManager.generateTwoFASecret(user);
        sendVerificationCodeBySMS(user);
    }

    /**
     * Checks if verification code is valid and complete SMS-Based MFA activation.
     *
     * @param user             User will be enable MFA
     * @param verificationCode Validation Code
     */
    @Override
    public void verify(DocutoolsUser user, String verificationCode) {
        UserSettings settings = user.getSettings();
        Totp totp = new Totp(settings.getTwoFASecret(), clock);

        try {
            // totp.verify can throw exception when code is wrong type
            if (!totp.verify(verificationCode)) {
                // Throwing error when code is invalid
                throw new IllegalArgumentException("Invalid Verification Code");
            }

            userManager.enableSMSFactorAuthentication(user);
        } catch (Exception e) {
            throw ExceptionHelper.newBadRequestError(ErrorCodes.INVALID_VERIFICATION_CODE, "Invalid Verification Code");
        }
    }

    /**
     * Disable the sending of the verification code by SMS e-mail to the user
     *
     * @param user User will be disable MFA
     */
    @Override
    public void deactivate(DocutoolsUser user) {
        userManager.disableSMSFactorAuthentication(user);
    }

    /**
     * Send SMS message to used with verification code valid for 5 minutes
     *
     * @param user User should receive the verification code.
     */
    @Override
    public void sendVerificationCodeBySMS(DocutoolsUser user) {
        String phoneNumber = user.getPhone();
        if (StringUtils.isEmpty(phoneNumber)) {
            throw ExceptionHelper.newMissingRequiredValueError("Phone number");
        }

        UserSettings settings = user.getSettings();
        String twoFASecret = settings.getTwoFASecret();
        if (StringUtils.isEmpty(twoFASecret)) {
            throw ExceptionHelper.newMissingRequiredValueError("FA secret");
        }

        Totp totp = new Totp(twoFASecret, clock);
        String code = totp.now();
        String message = String.format("Your verification code is %s", code);

        smsSender.sendSMSMessage(message, phoneNumber);
    }

}
