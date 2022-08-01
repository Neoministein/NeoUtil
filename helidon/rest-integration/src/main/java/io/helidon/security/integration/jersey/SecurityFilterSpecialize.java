package io.helidon.security.integration.jersey;

import io.helidon.security.SecurityContext;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * This class is used to override the base Security implementation of Helidon in order to use CDI for authentication and
 * to make it easier to port code from one implementation to another.
 */
@Priority(1200)
@ApplicationScoped
@ConstrainedTo(RuntimeType.SERVER)
public class SecurityFilterSpecialize extends SecurityFilter {

    @Override
    protected void doFilter(ContainerRequestContext request, SecurityContext securityContext) {
        //Look at class Javadoc
    }
}