package com.neo.util.common.impl;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.InternalRuntimeException;
import com.neo.util.common.impl.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class KeyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyUtils.class);

    private static final ExceptionDetails EX_RSA_KEY_PARSING = new ExceptionDetails(
            "common/rsa", "Invalid RSA {0} key.");

    private static final String PUBLIC_KEY = "public";
    private static final String PRIVATE_KEY = "private";

    private KeyUtils() {}

    /**
     * Parses a base64 encoded public key to a key instance
     *
     * @param base64Key the base64 encoded key
     * @return a public key instance
     *
     * @throws InternalRuntimeException if the key cannot be parsed
     */
    public static PublicKey parseRSAPublicKey(String base64Key) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64Key));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Invalid public key parsing format");
            throw new ValidationException(EX_RSA_KEY_PARSING, PUBLIC_KEY);
        } catch (InvalidKeySpecException ex) {
            LOGGER.error("RSA public key cannot be parsed");
            throw new ValidationException(EX_RSA_KEY_PARSING, PUBLIC_KEY);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("RSA public key is not Base64 encoded");
            throw new ValidationException(EX_RSA_KEY_PARSING, PUBLIC_KEY);
        }
    }

    /**
     * Parses a base64 encoded private key to a key instance
     *
     * @param base64Key the base64 encoded key
     * @return a private key instance
     */
    public static PrivateKey parseRSAPrivateKey(String base64Key) {
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64Key));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Invalid private key parsing format");
            throw new ValidationException(EX_RSA_KEY_PARSING, PRIVATE_KEY);
        } catch (InvalidKeySpecException ex) {
            LOGGER.error("RSA private key cannot be parsed");
            throw new ValidationException(EX_RSA_KEY_PARSING, PRIVATE_KEY);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("RSA private key is not Base64 encoded");
            throw new ValidationException(EX_RSA_KEY_PARSING, PRIVATE_KEY);
        }
    }
}
