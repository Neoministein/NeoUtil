package com.neo.util.framework.rest.impl.security;

import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.CredentialsGenerator;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.security.enterprise.credential.Credential;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Optional;

class AuthenticationFilterTest {


    AuthenticationFilter subject;

    AuthenticationProvider authenticationProvider;
    ResponseGenerator responseGenerator;
    CredentialsGenerator credentialsGenerator;
    HttpRequestDetails requestDetails;

    @BeforeEach
    void init() {
        subject = Mockito.spy(AuthenticationFilter.class);

        authenticationProvider = Mockito.mock(AuthenticationProvider.class);
        subject.authenticationProvider = authenticationProvider;

        responseGenerator = Mockito.mock(ResponseGenerator.class);
        subject.responseGenerator = responseGenerator;

        credentialsGenerator = Mockito.mock(CredentialsGenerator.class);
        subject.credentialsGenerator = credentialsGenerator;

        requestDetails = new HttpRequestDetails(null, null, null);
        subject.requestDetails = requestDetails;
    }

    @Test
    void authenticationSuccess() {
        //Arrange
        String httpHeader = "A Header";

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.doReturn(httpHeader).when(containerRequestContext).getHeaderString(HttpHeaders.AUTHORIZATION);

        Credential credential = Mockito.mock(Credential.class);
        Mockito.doReturn(credential).when(credentialsGenerator).generate(httpHeader);

        RolePrincipal rolePrincipal = Mockito.mock(RolePrincipal.class);
        Optional<RolePrincipal> rolePrincipalOptional = Optional.of(rolePrincipal);

        Mockito.doReturn(rolePrincipalOptional).when(authenticationProvider).authenticate(credential);
        //Act

        subject.filter(containerRequestContext);
        //Assert

        Assertions.assertTrue(requestDetails.getUser().isPresent());
        Mockito.verify(containerRequestContext, Mockito.times(0)).abortWith(Mockito.any());
    }

    @Test
    void authenticationFailure() {
        //Arrange
        String httpHeader = "A Header";

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.doReturn(httpHeader).when(containerRequestContext).getHeaderString(HttpHeaders.AUTHORIZATION);

        Credential credential = Mockito.mock(Credential.class);
        Mockito.doReturn(credential).when(credentialsGenerator).generate(httpHeader);

        Mockito.doReturn(Optional.empty()).when(authenticationProvider).authenticate(credential);
        //Act

        subject.filter(containerRequestContext);
        //Assert

        Assertions.assertTrue(requestDetails.getUser().isEmpty());
    }

    @Test
    void authenticationInvalidCredentials() {
        //Arrange
        String httpHeader = "A Header";

        ContainerRequestContext containerRequestContext = Mockito.mock(ContainerRequestContext.class);
        Mockito.doReturn(httpHeader).when(containerRequestContext).getHeaderString(HttpHeaders.AUTHORIZATION);

        Mockito.doThrow(new IllegalArgumentException()).when(credentialsGenerator).generate(httpHeader);

        //Act

        subject.filter(containerRequestContext);
        //Assert
        Assertions.assertTrue(requestDetails.getUser().isEmpty());
    }
}
