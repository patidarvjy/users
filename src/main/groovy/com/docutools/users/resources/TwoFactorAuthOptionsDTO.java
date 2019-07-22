package com.docutools.users.resources;

public class TwoFactorAuthOptionsDTO {

    private boolean otp;
    private boolean sms;

    public TwoFactorAuthOptionsDTO(boolean otp, boolean sms) {
        this.otp = otp;
        this.sms = sms;
    }

    public boolean isOtp() {
        return otp;
    }

    public boolean isSms() {
        return sms;
    }
}
