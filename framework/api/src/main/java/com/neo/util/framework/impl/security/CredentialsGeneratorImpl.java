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
            String content = httpHeader.substring(index + 1);
            return generate(httpScheme, content);
        }
        throw new IllegalStateException("Unsupported header scheme");
    }

    protected Credential generate(String httpScheme, String content) {
        if (authenticationProvider.getSupportedAuthenticationSchemes().contains(httpScheme)) {
            switch (httpScheme) {
            case AuthenticationScheme.BASIC:
                return new BasicAuthenticationCredential(content);
            case AuthenticationScheme.BEARER:
                return new BearerCredentials(content);
            default:
            }
        }
        throw new IllegalStateException("Unsupported header scheme");
    }
}
