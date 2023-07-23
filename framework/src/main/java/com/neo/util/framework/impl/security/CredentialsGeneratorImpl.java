package com.neo.util.framework.impl.security;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.AuthenticationScheme;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.credential.BearerCredentials;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.BasicAuthenticationCredential;
import jakarta.security.enterprise.credential.Credential;

@ApplicationScoped
public class CredentialsGeneratorImpl implements CredentialsGenerator {

    public static final ExceptionDetails EX_BASIC_INVALID = new ExceptionDetails(
            "auth/basic-invalid", "The provided basic token is invalid", false);

    @Inject
    protected AuthenticationProvider authenticationProvider;

    @Override
    public Credential generate(String httpHeader) {
        return generate(getHttpScheme(httpHeader), httpHeader);
    }

    protected Credential generate(String httpScheme, String httpHeader) {
        if (authenticationProvider.getSupportedAuthenticationSchemes().contains(httpScheme.toUpperCase())) {
            return switch (httpScheme.toUpperCase()) {
                case AuthenticationScheme.BASIC -> new BasicAuthenticationCredential(httpHeader);
                case AuthenticationScheme.BEARER -> new BearerCredentials(httpHeader);
                default -> throw new ValidationException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
            };
        }
        throw new ValidationException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
    }

    protected String getHttpScheme(String httpHeader) {
        if (StringUtils.isEmpty(httpHeader)) {
            throw new ValidationException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
        }

        int index = httpHeader.indexOf(' ');
        if (index >= 0) {
            return httpHeader.substring(0 ,index);
        }
        throw new ValidationException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
    }

    protected BasicAuthenticationCredential parseBasicCredenital(String httpHeader) {
        try {
            return new BasicAuthenticationCredential(httpHeader);
        } catch (RuntimeException ex) {
            throw new ValidationException(EX_BASIC_INVALID);
        }
    }
}
