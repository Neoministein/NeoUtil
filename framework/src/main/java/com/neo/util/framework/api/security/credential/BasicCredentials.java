package com.neo.util.framework.api.security.credential;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;

import java.util.Base64;

/**
 * Credentials implementation for Basic
 */
public class BasicCredentials extends UsernamePasswordCredential {

    public static final ExceptionDetails EX_BASIC_INVALID = new ExceptionDetails("auth/basic-invalid",
            "The provided basic schema is invalid");

    public BasicCredentials(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.toUpperCase().startsWith("BASIC ")) {
            throw new ValidationException(EX_BASIC_INVALID);
        }
        String encodedCredentials = authorizationHeader.substring("BASIC ".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(encodedCredentials.getBytes()));
        String[] delimiterIndex = credentials.split(":");
        if (delimiterIndex.length != 2) {
            throw new ValidationException(EX_BASIC_INVALID);
        }

        super(delimiterIndex[0], delimiterIndex[1]);
    }
}
