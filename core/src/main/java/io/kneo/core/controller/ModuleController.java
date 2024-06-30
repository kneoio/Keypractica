
package io.kneo.core.controller;


import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.model.Module;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.ModuleService;
import io.kneo.core.service.UserService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class ModuleController extends AbstractSecuredController<Module, ModuleDTO> {

    @Inject
    ModuleService service;

    public ModuleController(UserService userService) {
        super(userService);
    }

    @GET
    @Path("/")
    @JsonView(Views.ListView.class)
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Valid @Min(0) @QueryParam("size") int size, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        return getAll(service, requestContext, page, size);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        return getDocument(service, id);
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid ModuleDTO dto) {
        return service.add(dto)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @PUT
    @Path("/")
    public Response update(@Valid ModuleDTO dto) {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
