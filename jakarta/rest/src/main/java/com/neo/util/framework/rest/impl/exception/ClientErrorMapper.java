package com.neo.util.framework.rest.impl.exception;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.rest.impl.security.AuthenticationFilter;
import com.neo.util.framework.rest.impl.security.IdentificationFilter;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client error mapper returns the provided response by the {@link ClientErrorException}
 *
 * This is used for 404 errors or invalid HTTP method
 */
@Provider
@RequestScoped
public class ClientErrorMapper implements ExceptionMapper<ClientErrorException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientErrorMapper.class);

    @Context
    protected UriInfo uriInfo;

    @Context
    protected ContainerRequestContext context;

    @Inject
    protected jakarta.inject.Provider<RequestDetails> requestDetailsProvider;

    @Inject
    protected IdentificationFilter requestIdentificationFilter;

    @Inject
    protected AuthenticationFilter authenticationFilter;

    @Override
    public Response toResponse(ClientErrorException ex) {
        //Manually running through the filters for logging since the request gets aborted before we even get to them

        RequestDetails requestDetails = requestDetailsProvider.get();
        if (requestDetails == null) {
            requestIdentificationFilter.filter(context);
            authenticationFilter.filter(context);
            requestDetails = requestDetailsProvider.get();
        }

        if (ex instanceof NotFoundException || ex instanceof NotAllowedException) {
            LOGGER.warn("A [{}] occurred with message [{}] setting status to [{}]",
                    ex.getClass().getSimpleName(),requestDetails.getRequestContext(), ex.getResponse().getStatus());
        } else {
            LOGGER.warn("A [{}] occurred with message [{}] setting status to [{}]",
                    ex.getClass().getSimpleName(), ex.getMessage(), ex.getResponse().getStatus());
        }

        return ex.getResponse();
    }
}
