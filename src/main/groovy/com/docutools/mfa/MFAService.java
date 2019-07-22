package com.docutools.mfa;

import com.docutools.users.DocutoolsUser;

/**
 * Interface for Service Multi Factor Authentication (MFA). It can be used to several implementation types of MFA,
 * like SMS-Based, Phone-Call Based, etc.
 */
public interface MFAService {

    /**
     * Request to active MFA to specific user.
     * @param user User will be enable MFA
     */
    void active(DocutoolsUser user);

    /**
     * Verification to active MFA for the user
     * @param user User will be enable MFA
     * @param verificationCode Validation Code
     */
    void verify(DocutoolsUser user, String verificationCode);

    /**
     * Disable MFA for the user
     * @param user User will be disable MFA
     */
    void deactivate(DocutoolsUser user);

    /**
     * Send verification code to the user, using the channel implemented by this class.
     * @param user User should receive the verification code.
     */
    void sendVerificationCodeBySMS(DocutoolsUser user);
}
