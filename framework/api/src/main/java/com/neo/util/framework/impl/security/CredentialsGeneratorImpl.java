package com.neo.util.framework.impl.security;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.AuthenticationScheme;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.credential.BearerCredentials;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.BasicAuthenticationCredential;
import javax.security.enterprise.credential.Credential;

@ApplicationScoped
public class CredentialsGeneratorImpl implements CredentialsGenerator {

    @Inject
    protected AuthenticationProvider authenticationProvider;

    @Override
    public Credential generate(String httpHeader) {
        if (StringUtils.isEmpty(httpHeader)) {
            throw new IllegalArgumentException("Header cannot be empty");
        }

        int index = httpHeader.indexOf(' ');
        if (index >= 0) {
            String httpScheme = httpHeader.substring(0 ,index);
            return generate(httpScheme, httpHeader);
        }
        throw new IllegalStateException("Unsupported header scheme");
    }

    protected Credential generate(String httpScheme, String httpHeader) {
        if (authenticationProvider.getSupportedAuthenticationSchemes().contains(httpScheme.toUpperCase())) {
            switch (httpScheme.toUpperCase()) {
            case AuthenticationScheme.BASIC:
                return new BasicAuthenticationCredential(httpHeader);
            case AuthenticationScheme.BEARER:
                return new BearerCredentials(httpHeader);
            default:
            }
        }
        throw new IllegalStateException("Unsupported header scheme");
    }
}
