package io.kneo.officeframe.controller;


import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.officeframe.dto.OrgCategoryDTO;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.service.OrgCategoryService;
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

import java.util.Optional;

@Path("/orgcategories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class OrgCategoryController extends AbstractSecuredController<OrgCategory, OrgCategoryDTO> {
    @Inject
    OrgCategoryService service;

    @GET
    @Path("/")
    @PermitAll
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        return getById(service, id, requestContext);
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid OrgCategoryDTO dto, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        return create(service, dto, requestContext);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, OrgCategoryDTO dto, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        return update(id, service, dto, requestContext);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Integer> delete(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            return service.delete(id, user);
        }
        return null;
    }

}
