package com.neo.util.framework.api.security.credential;

import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.security.AuthenticationScheme;
import jakarta.security.enterprise.credential.AbstractClearableCredential;

/**
 * Credentials implementation for Bearer
 */
public class BearerCredentials extends AbstractClearableCredential {

    public static final ExceptionDetails EX_BEARER_INVALID = new ExceptionDetails(
            "auth/bearer-invalid", "The provided bearer token is invalid", false);

    protected String token;

    public BearerCredentials(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.toUpperCase()
                .startsWith(AuthenticationScheme.BEARER.toUpperCase() + " ")) {
            token = authorizationHeader.substring(AuthenticationScheme.BEARER.length()).trim();
        } else {
            throw new ValidationException(EX_BEARER_INVALID);
        }

    }

    public String getToken() {
        return token;
    }

    @Override
    protected void clearCredential() {
        token = null;
    }
}
