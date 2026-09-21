package com.neo.util.jakarta.security;

import com.neo.util.api.request.UserRequestDetails;
import com.neo.util.api.request.user.RolePrincipal;
import com.neo.util.api.security.AuthenticationProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DummyAuthenticationProvider implements AuthenticationProvider {

    @Override
    public boolean isSecurityEnabled() {
        return false;
    }

    @Override
    public Optional<RolePrincipal> authenticate(Credential credential) {
        return Optional.empty();
    }

    @Override
    public void authenticate(UserRequestDetails userRequestDetails,
            Credential credential) {
    }


    @Override
    public List<String> getSupportedAuthenticationSchemes() {
        return List.of();
    }
}
