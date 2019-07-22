package com.docutools.config.security

/**
 * Generates links to QR Codes for connecting Google Authenticator using the Google Charts API.
 */
class OTPQRCodeLinkGeneration {

    static final APP_NAME = "docutools"
    static final QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl="

    /**
     * Generates a QR Code link using Google Charts API for the users email and TOTP secret.
     *
     * @param email user's email
     * @param secret user's secret
     * @return qr code link
     */
    def static String createQRCodeLink(String email, String secret) {
        "${QR_PREFIX}otpauth://totp/${APP_NAME}:${email}?secret=${secret}&issuer=${APP_NAME}"
    }

}
