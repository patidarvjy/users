package com.docutools.config.security


import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

import static com.docutools.exceptions.ExceptionHelper.*;
/**
 * Creates QR Codes using Google's ZXing library. Shall replace dependency on Google's Graph API.
 */
class QRCodeGenerator {

    static final APP_NAME = 'docutools'
    static final QR_SIZE = 200

    /**
     * Generates a QR code that links to the otpauth for the specified email and secret and {@code docutools} as issuer.
     *
     * The QR code is written as 200x200px PNG to the specified output stream.
     *
     * @param email user's docutools login email address.
     * @param secret user's two factor secret.
     * @param os the IO output stream the generated QR code rendered to, as PNG.
     * @throws com.docutools.apierrors.ApiException when there is some issue with rendering QR code to response.
     */
    static void writeToOutputStream(String email, String secret, OutputStream os) {
        // Create QR bit matrix
        def content = "otpauth://totp/${APP_NAME}:${email}?secret=${secret}&issuer=${APP_NAME}"
        def writer = new QRCodeWriter()
        def bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, qrCodeHints)

        try {
            // Write QR code as PNG image to output stream
            MatrixToImageWriter.writeToStream(bitMatrix, 'PNG', os)
            os.flush()
        } catch (IOException e) {
            throw newInternalServerError('Some issue occurred when trying to render QR code to response.', e)
        }
    }

    private static Map<EncodeHintType, Object> getQrCodeHints() {
        def hints = new EnumMap<EncodeHintType, Object>(EncodeHintType)
        hints.put(EncodeHintType.CHARACTER_SET, 'UTF-8')
        hints.put(EncodeHintType.MARGIN, 1)
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
        return hints
    }

}
