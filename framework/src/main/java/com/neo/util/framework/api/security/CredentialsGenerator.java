package com.neo.util.framework.api.security;

import com.neo.util.common.impl.exception.ValidationException;
import jakarta.security.enterprise.credential.Credential;

/**
 * Parses incoming request data to credentials
 */
public interface CredentialsGenerator {

    /**
     * Parses incoming header to a {@link Credential} object.
     *
     * @param httpHeader incoming http header
     *
     * @return a valid credential object
     *
     * @throws IllegalArgumentException the content isn't valid or according to the scheme
     * @throws IllegalStateException when the Http scheme isn't supported
     */
    Credential generate(String httpHeader) throws ValidationException;
}
