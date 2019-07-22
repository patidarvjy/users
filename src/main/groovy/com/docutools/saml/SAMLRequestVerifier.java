package com.docutools.saml;

import com.docutools.exceptions.ErrorCodes;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static com.docutools.exceptions.ExceptionHelper.*;

/**
 * Verifies given {@link SAMLAuthenticationRequest}s using the public key configured in the file {@code docutools.saml.keyPath}
 * or in the classpath resource {@code docutools.saml.keyResource} (path is prioritized over resource).
 *
 * The verification algorithm used is SHA1 with RSA. The signature data is {@code req.getEmail() + " +++ " + req.getIdP()}.
 *
 * @author amp, munish
 */
@Service
public class SAMLRequestVerifier {

    private static final Logger log = LoggerFactory.getLogger(SAMLRequestVerifier.class);

    private static  final String ENCRYPTION_ALGORITHM = "RSA";
    private static  final String HASH_ENCRYPTION_ALGORITHM = "SHA1withRSA";
    private static  final String CHARSET = "UTF-8";

    private PublicKey publicKey;

    public SAMLRequestVerifier(@Value("${docutools.saml.keyResource:}") String keyResource,
                               @Value("${docutools.saml.keyPath:}") String keyPath) {
        try {
            this.publicKey = loadKey(loadKeyResource(keyResource, keyPath));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            log.error("Could not load Public Key for SAML Request Verification!", e);
            throw new BeanCreationException("Could not load Public Key for SAML Request Verification!", e);
        }
    }

    private PublicKey loadKey(Resource keyResource) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException{
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        byte[] pubKey;
        X509EncodedKeySpec publicKeySpec;
        try (PemReader reader = new PemReader(new InputStreamReader(keyResource.getInputStream()))) {
            pubKey = reader.readPemObject().getContent();
        }
        publicKeySpec = new X509EncodedKeySpec(pubKey);
        return keyFactory.generatePublic( publicKeySpec );
    }

    private Resource loadKeyResource(String keyResource, String keyPath) {
        if(!StringUtils.isEmpty(keyPath) && new File(keyPath).exists()) {
            return new FileSystemResource(keyPath);
        }
        if(!StringUtils.isEmpty(keyResource)) {
            return new ClassPathResource(keyResource);
        }
        throw new BeanCreationException("SAMLRequestVerifier required docutools.saml.keyResource to point to a " +
                "Classpath Resource or docutools.saml.keyPath to an existing file!");
    }

    /**
     * Verifies the signature of the given {@link SAMLAuthenticationRequest} with the configured public key.
     *
     * @param request the {@link SAMLAuthenticationRequest}
     * @return {@code true} when signature is OK
     */
    public boolean verify(SAMLAuthenticationRequest request) {
        try {
            Signature signature = Signature.getInstance(HASH_ENCRYPTION_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(request.toSignatureMessage().getBytes(CHARSET));
            byte[] bytes = Base64.decode(request.getSign().getBytes(CHARSET));
            return signature.verify(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
            log.error("An Exception was thrown during SAML Request Signature Validation!", e);
            throw newBadRequestError(ErrorCodes.SIGNITURE_VALIDATION_ERROR,  e);
        }
    }

}
