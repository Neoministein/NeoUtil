package com.neo.util.framework.impl.security;

import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.RolePrincipal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DummyAuthenticationProvider implements AuthenticationProvider {

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
