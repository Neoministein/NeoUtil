package com.neo.util.helidon.rest.security;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.request.UserRequestDetails;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.AuthenticationScheme;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.api.security.credential.BearerCredentials;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.security.enterprise.credential.Credential;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Alternative
@Priority(PriorityConstants.TEST)
@ApplicationScoped
public class BasicAuthorizationProvider implements AuthenticationProvider {

    public static final String NORMAL_TOKEN = "ABCDEFGHIJKLMNOPQRSTUFWXYZ";
    public static final RolePrincipal NORMAL_PRINCIPAL = new RolePrincipal() {
        @Override
        public Set<String> getRoles() {
            return Set.of();
        }

        @Override
        public String getName() {
            return "TEST_USER";
        }
    };

    public static final String ADMIN_TOKEN = "0123456789";
    public static final RolePrincipal ADMIN_PRINCIPAL = new RolePrincipal() {
        @Override
        public Set<String> getRoles() {
            return Set.of("ADMIN");
        }

        @Override
        public String getName() {
            return "ADMIN_USER";
        }
    };

    @Override
    public Optional<RolePrincipal> authenticate(Credential credential) {
        if (credential instanceof BearerCredentials) {
            if (NORMAL_TOKEN.equals(((BearerCredentials) credential).getToken())) {
                return Optional.of(NORMAL_PRINCIPAL);
            }
            if (ADMIN_TOKEN.equals(((BearerCredentials) credential).getToken())) {
                return Optional.of(ADMIN_PRINCIPAL);
            }
        }
        return Optional.empty();
    }

    @Override
    public void authenticate(UserRequestDetails userRequestDetails, Credential credential) {
        authenticate(credential).ifPresent(userRequestDetails::setUserIfPossible);
    }

    @Override
    public List<String> getSupportedAuthenticationSchemes() {
        return List.of(AuthenticationScheme.BEARER);
    }
}
