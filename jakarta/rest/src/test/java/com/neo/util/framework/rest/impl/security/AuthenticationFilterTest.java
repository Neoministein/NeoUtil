package com.neo.util.framework.rest.impl.security;

import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.security.AuthenticationProvider;
import com.neo.util.framework.api.security.HttpCredentialsGenerator;
import com.neo.util.framework.impl.security.HttpCredentialsGeneratorImpl;
import com.neo.util.framework.rest.api.request.HttpRequestDetails;
import com.neo.util.framework.rest.api.response.ClientResponseService;
import jakarta.security.enterprise.credential.Credential;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class AuthenticationFilterTest {


    AuthenticationFilter subject;

    AuthenticationProvider authenticationProvider;
    HttpCredentialsGenerator credentialsGenerator;
    HttpRequestDetails requestDetails;

    @BeforeEach
    void init() {
        subject = Mockito.spy(AuthenticationFilter.class);

        authenticationProvider = Mockito.mock(AuthenticationProvider.class);
        subject.authenticationProvider = authenticationProvider;

        credentialsGenerator = Mockito.mock(HttpCredentialsGenerator.class);
        subject.httpCredentialsGenerator = credentialsGenerator;

        requestDetails = new HttpRequestDetails(null, null, null, null, new HttpRequestDetails.Context(null, null));
        subject.requestDetails = requestDetails;
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

        Mockito.doThrow(new ValidationException(HttpCredentialsGeneratorImpl.EX_BASIC_INVALID)).when(credentialsGenerator).generate(httpHeader);

        //Act

        subject.filter(containerRequestContext);
        //Assert
        Assertions.assertTrue(requestDetails.getUser().isEmpty());
    }
}
