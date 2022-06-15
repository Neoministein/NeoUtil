package com.neo.util.framework.rest.impl;

import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.api.connection.RequestContext;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.rest.api.RestAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.util.List;

class RestActionProcessorTest {

    public static final RequestContext CURRENT_CONTEXT = new RequestContext("GET","testMethod");

    RestActionProcessor subject;

    RequestDetails requestDetails;

    @BeforeEach
    public void init() {
        subject = Mockito.spy(RestActionProcessor.class);

        requestDetails = Mockito.mock(RequestDetails.class);
        subject.requestDetails = requestDetails;
        Mockito.doReturn(CURRENT_CONTEXT).when(requestDetails).getRequestContext();
    }

    @Test
    void handlesJSONExceptionTest() {
        //Arrange
        RestAction restAction = () -> {
            throw new InternalJsonException();
        };
        //Act / Assert
        Response response = Assertions.assertDoesNotThrow(() -> subject.process(restAction));
        Assertions.assertEquals(
                DefaultResponse.error(400, requestDetails.getRequestContext(), RestActionProcessor.E_INVALID_JSON, "Invalid json format in the request body " + CURRENT_CONTEXT).toString(),
                response.toString());
    }

    @Test
    void handlesInternalLogicExceptionTest() {
        //Arrange
        RestAction restAction = () -> {
            throw new InternalLogicException();
        };
        //Act / Assert
        Response response = Assertions.assertDoesNotThrow(() -> subject.process(restAction));
        assertResponse(
                DefaultResponse.error(500, RestActionProcessor.E_INTERNAL_LOGIC, requestDetails.getRequestContext()),
                response);
    }

    @Test
    void handlesExceptionTest() {
        //Arrange
        RestAction restAction = () -> {
            throw new RuntimeException();
        };
        //Act / Assert
        Response response = Assertions.assertDoesNotThrow(() -> subject.process(restAction));

        assertResponse(
                DefaultResponse.error(500, RestActionProcessor.E_INTERNAL_LOGIC, requestDetails.getRequestContext()),
                response);
    }

    @Test
    void defaultActionTest() {
        //Arrange
        RestAction restAction = () -> Response.ok().build();
        //Act
        Response response = subject.process(restAction);
        //Assert
        assertResponse(Response.ok().build(), response);
    }

    @Test
    void actionRequiresPermission() {
        //Arrange
        Mockito.doReturn(true).when(requestDetails).isInRoles(List.of("test"));

        RestAction restAction = () -> Response.ok().build();
        //Act

        Response response = subject.process(restAction, List.of("test"));
        //Assert
        assertResponse(Response.ok().build(), response);
    }


    @Test
    void actionMissingPermission() {
        //Arrange
        Mockito.doReturn(false).when(requestDetails).isInRoles(List.of("test"));

        RestAction restAction = () -> Response.ok().build();
        //Act

        Response response = subject.process(restAction, List.of("test"));
        //Assert
        assertResponse(
                DefaultResponse.error(403, RestActionProcessor.E_FORBIDDEN, requestDetails.getRequestContext()),
                response);
    }

    public void assertResponse(Response expected, Response actual) {
        Assertions.assertEquals(expected.toString(), actual.toString());
        Assertions.assertEquals(
                expected.getEntity(),
                actual.getEntity());
    }
}
