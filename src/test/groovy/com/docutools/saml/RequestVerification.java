package com.docutools.saml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.DataInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
public class RequestVerification {

    private PrivateKey privateKey;
    private SAMLRequestVerifier verifier;

    @BeforeEach
    public void setup() throws Exception {
        this.privateKey = loadPrivateKey(new ClassPathResource("saml/private_key.der"));
        this.verifier = new SAMLRequestVerifier("saml/public_key.pem", null);
    }

    private static PrivateKey loadPrivateKey(Resource resource) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] privKeyBytes = new byte[(int)resource.contentLength()];
        try(DataInputStream in = new DataInputStream(resource.getInputStream())) {
            in.read(privKeyBytes);
        }
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privKeyBytes);
        return keyFactory.generatePrivate(privSpec);
    }

    @Test
    public void verifiySignature() throws Exception {
        // Arrange
        String email = "post.malone@gmail.com";
        String idp = "https://google.com/saml/metadata.xml";
        String message = String.format("%s +++ %s", email, idp);
        String signature = sign(message);
        SAMLAuthenticationRequest request = new SAMLAuthenticationRequest(null, email, idp, signature,  null, null, null, null);
        // Act
        boolean verified = verifier.verify(request);
        // Assert
        assertTrue(verified, String.format("Signature Miss Match: %s => %s", message, signature));
    }

    @Test
    public void failVerification() throws Exception {
        // Arrange
        String email = "mix_tapes@protonmail.com";
        String idp = "https://protonmail.com/metadata.xml";
        String message = String.format("%s +++ %s", email, idp);
        String signature = sign(message);
        SAMLAuthenticationRequest request = new SAMLAuthenticationRequest(null, "yoyo@protonmail.com", idp, signature, null, null, null, null);
        // Act
        boolean verified = verifier.verify(request);
        // Assert
        assertFalse(verified, String.format("Signature should not have matched! %s => %s", request.toSignatureMessage(), signature));
    }

    private String sign(String message) throws Exception {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes("UTF-8"));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

}
