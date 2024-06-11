package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.model.user.IUser;
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
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Valid @Min(0) @QueryParam("size") int size, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page, size);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        return getById(service, id, requestContext);
    }

    @POST
    @Path("/")
    public Uni<Response> create(OrganizationDTO dto, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.add(dto, userOptional.get())
                    .onItem().transform(id -> Response.created(URI.create("/orgs/" + id)).build());
        } else {
            return Uni.createFrom()
                    .item(postForbidden(getUserOIDCName(requestContext)));

        }
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, OrganizationDTO dto, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.update(id, dto, userOptional.get())
                    .onItem().transform(count -> count > 0 ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
        } else {
            return Uni.createFrom()
                    .item(postForbidden(getUserOIDCName(requestContext)));

        }
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.delete(id, userOptional.get())
                    .onItem().transform(count -> count > 0 ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build());
        } else {
            return Uni.createFrom()
                    .item(postForbidden(getUserOIDCName(requestContext)));

        }
    }
}
