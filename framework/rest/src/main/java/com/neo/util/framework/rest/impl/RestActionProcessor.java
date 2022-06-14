package com.neo.util.framework.rest.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.api.connection.RequestDetails;
import com.neo.util.framework.rest.api.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;

@RequestScoped
public class RestActionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestActionProcessor.class);

    public static final ObjectNode E_INTERNAL_LOGIC = DefaultResponse.errorObject("unknown","Internal server error please try again later");
    public static final ObjectNode E_UNAUTHORIZED = DefaultResponse.errorObject("auth/000", "Unauthorized");
    public static final ObjectNode E_FORBIDDEN = DefaultResponse.errorObject("auth/001", "Forbidden");

    protected static final String E_INVALID_JSON = "json/000";

    @Inject
    protected RequestDetails requestDetails;

    public Response process(RestAction restAction) {
        return process(restAction, List.of());
    }

    public Response process(RestAction restAction, List<String> requiredRoles) {
        if (!authorized(requiredRoles)) {
            LOGGER.debug("Unable to execute rest at {} action missing permissions {}",requestDetails.getRequestContext(), requiredRoles);
            return DefaultResponse.error(403, E_FORBIDDEN, requestDetails.getRequestContext());
        }

        Response response;
        try {
            LOGGER.debug("Executing action at {}", requestDetails.getRequestContext());
            response = restAction.run();
        } catch (InternalJsonException ex) {
            LOGGER.debug("Invalid json format in the request body");
            response = DefaultResponse.error(400, requestDetails.getRequestContext(), E_INVALID_JSON, "Invalid json format in the request body " + ex.getMessage());
        } catch (InternalLogicException ex) {
            LOGGER.error("A exception occurred during a rest call", ex);
            response = DefaultResponse.error(500, E_INTERNAL_LOGIC, requestDetails.getRequestContext());
        } catch (Exception ex) {
            LOGGER.error("A unexpected exception occurred during a rest call", ex);
            response = DefaultResponse.error(500, E_INTERNAL_LOGIC, requestDetails.getRequestContext());
        }
        return response;
    }

    public boolean authorized(List<String> requiredRoles) {
        return requestDetails.isInRoles(requiredRoles);
    }

    //Only for testing purposes
    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}
