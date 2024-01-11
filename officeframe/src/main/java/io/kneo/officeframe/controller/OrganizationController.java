package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.repository.exception.DocumentExistsException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.service.OrganizationService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;

@Path("/orgs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class OrganizationController extends AbstractSecuredController<Organization, OrganizationDTO> {
    @Inject
    OrganizationService service;
    @GET
    @Path("/")
    @PermitAll
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page);
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        Uni<Optional<Organization>> user = service.get(id);
        return Response.ok(user).build();
    }

    @POST
    @Path("/")
    public Response create(OrganizationDTO dto) throws DocumentExistsException {
        return Response.created(URI.create("/" + service.add(dto))).build();
    }

    @PUT
    @Path("/")
    public Response update(OrganizationDTO dto) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
