package com.neo.util.helidon.rest.entity;

import com.neo.util.framework.rest.impl.entity.AbstractEntityRestEndpoint;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(TestPersonResource.RESOURCE_LOCATION)
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RequestScoped
public class TestPersonResource extends AbstractEntityRestEndpoint<TestPersonEntity> {

    public static final String RESOURCE_LOCATION = "/test/entity/testuser";

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        return super.getByPrimaryKey(id);
    }

    @POST
    @Override
    public Response create(String x) {
        return super.create(x);
    }

    @PUT
    @Override
    public Response edit(String x) {
        return super.edit(x);
    }


    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String primaryKey) {
        return super.delete(primaryKey);
    }

    @Override
    protected Object convertToPrimaryKey(String primaryKey) {
        try {
            return Long.parseLong(primaryKey);
        } catch (NumberFormatException ex) {
            throw new ClientErrorException(404);
        }
    }

    @Override
    protected Class<TestPersonEntity> getEntityClass() {
        return TestPersonEntity.class;
    }
}
