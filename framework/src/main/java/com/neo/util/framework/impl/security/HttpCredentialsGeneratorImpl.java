package com.neo.util.framework.impl.security;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.AuthenticationScheme;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.api.security.credential.BearerCredentials;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.BasicAuthenticationCredential;
import jakarta.security.enterprise.credential.Credential;

@ApplicationScoped
public class HttpCredentialsGeneratorImpl implements HttpCredentialsGenerator {

    public static final ExceptionDetails EX_BASIC_INVALID = new ExceptionDetails(
            "auth/basic-invalid", "The provided basic token is invalid");

    protected final AuthenticationProvider authenticationProvider;

    @Inject
    public HttpCredentialsGeneratorImpl(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public Credential generate(String httpHeader) {
        return generate(getHttpScheme(httpHeader), httpHeader);
    }

    protected Credential generate(String httpScheme, String httpHeader) {
        if (authenticationProvider.getSupportedAuthenticationSchemes().contains(httpScheme.toUpperCase())) {
            return switch (httpScheme.toUpperCase()) {
                case AuthenticationScheme.BASIC -> new BasicAuthenticationCredential(httpHeader);
                case AuthenticationScheme.BEARER -> new BearerCredentials(httpHeader);
                default -> throw new ExternalRuntimeException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
            };
        }
        throw new ExternalRuntimeException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
    }

    protected String getHttpScheme(String httpHeader) {
        if (StringUtils.isEmpty(httpHeader)) {
            throw new ExternalRuntimeException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
        }

        int index = httpHeader.indexOf(' ');
        if (index >= 0) {
            return httpHeader.substring(0 ,index);
        }
        throw new ExternalRuntimeException(FrameworkConstants.EX_UNSUPPORTED_AUTH_TYPE);
    }

    protected BasicAuthenticationCredential parseBasicCredenital(String httpHeader) {
        try {
            return new BasicAuthenticationCredential(httpHeader);
        } catch (RuntimeException ex) {
            throw new ExternalRuntimeException(EX_BASIC_INVALID);
        }
    }
}