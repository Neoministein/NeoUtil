package com.neo.util.framework.impl.security;

import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.RolePrincipal;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PretendAuthenticationProvider implements AuthenticationProvider {

    @Override public Optional<RolePrincipal> authenticate(Credential credential) {
        return Optional.empty();
    }

    @Override
    public List<String> getSupportedAuthenticationSchemes() {
        return List.of();
    }
}
