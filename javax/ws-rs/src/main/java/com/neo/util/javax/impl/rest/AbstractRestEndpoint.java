package com.neo.util.javax.impl.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.common.impl.exception.InternalJsonException;
import com.neo.common.impl.exception.InternalLogicException;
import com.neo.javax.api.connection.RequestDetails;
import com.neo.util.javax.api.rest.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;

public abstract class AbstractRestEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestEndpoint.class);

    public static final ObjectNode E_INTERNAL_LOGIC = DefaultResponse.errorObject("unknown","Internal server error please try again later");
    public static final ObjectNode E_UNAUTHORIZED = DefaultResponse.errorObject("auth/000", "Unauthorized");
    public static final ObjectNode E_FORBIDDEN = DefaultResponse.errorObject("auth/001", "Forbidden");

    protected static final String E_INVALID_JSON = "json/000";

    protected static final String E_INTERNAL = "internal";

    @Inject
    protected RequestDetails requestDetails;

    public Response restCall(RestAction restAction, RequestContext context) {
        return restCall(restAction, context, List.of());
    }

    public Response restCall(RestAction restAction, RequestContext context, List<String> requiredRoles) {
        MDC.put("traceId", requestDetails.getRequestId());
        LOGGER.debug("{} {}", context, requestDetails.getRemoteAddress());

        if (!authorized(requiredRoles)) {
            return DefaultResponse.error(403, E_FORBIDDEN, context);
        }
        try {
            return restAction.run();
        } catch (InternalJsonException ex) {
            LOGGER.debug("Invalid json format in the request body");
            return DefaultResponse.error(400, context, E_INVALID_JSON, "Invalid json format in the request body " + ex.getMessage());
        } catch (InternalLogicException ex) {
            LOGGER.error("A exception occurred during a rest call", ex);
            return DefaultResponse.error(500, E_INTERNAL_LOGIC, context);
        } catch (Exception ex) {
            LOGGER.error("A unexpected exception occurred during a rest call", ex);
            return DefaultResponse.error(500, E_INTERNAL_LOGIC, context);
        }
    }

    public boolean authorized(List<String> requiredRoles) {
        return requestDetails.isInRoles(requiredRoles);
    }

    protected abstract String getClassURI();

    public RequestContext getContext(HttpMethod method, String methodURI) {
        return new RequestContext(method, getClassURI(), methodURI);
    }

    //Only for testing purposes
    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }
}
