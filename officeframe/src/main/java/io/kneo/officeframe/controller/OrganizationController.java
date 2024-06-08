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
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

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
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Valid @Min(0) @QueryParam("size") int size, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page, size);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext)  {
        return getById(service, id, requestContext);
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
