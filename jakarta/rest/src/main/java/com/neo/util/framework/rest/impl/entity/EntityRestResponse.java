package com.neo.util.framework.rest.impl.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.framework.rest.api.response.ResponseGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EntityRestResponse {

    protected final ObjectNode notFoundError;
    protected final ObjectNode cannotParseError;
    protected final ObjectNode missingFieldsError;
    protected final ObjectNode notUniqueError;
    protected final ObjectNode invalidResourcePermission;

    @Inject
    public EntityRestResponse(ResponseGenerator responseGenerator) {
        notFoundError = responseGenerator.errorObject("resources/000","Entity not found");
        cannotParseError = responseGenerator.errorObject("resources/001","Unable to retrieve entity");
        missingFieldsError = responseGenerator.errorObject("resources/002","Entity is missing mandatory fields");
        notUniqueError = responseGenerator.errorObject("resources/003","Provided value isn't unique");
        invalidResourcePermission = responseGenerator.errorObject("resources/004","Invalid resource permission");
    }


    public ObjectNode getNotFoundError() {
        return notFoundError;
    }

    public ObjectNode getCannotParseError() {
        return cannotParseError;
    }

    public ObjectNode getMissingFieldsError() {
        return missingFieldsError;
    }

    public ObjectNode getNotUniqueError() {
        return notUniqueError;
    }

    public ObjectNode getInvalidResourcePermission() {
        return invalidResourcePermission;
    }
}
