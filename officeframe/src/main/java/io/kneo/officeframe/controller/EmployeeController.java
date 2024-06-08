package io.kneo.officeframe.controller;


import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.service.EmployeeService;
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

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class EmployeeController extends AbstractSecuredController<Employee, EmployeeDTO> {
    @Inject
    EmployeeService service;

    @GET
    @Path("/")
    @PermitAll
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Valid @Min(0) @QueryParam("size") int size, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page, size);
    }

    @GET
    @Path("/search/{keyword}")
    public Uni<Response> search(@PathParam("keyword") String keyword) {
        ViewPage viewPage = new ViewPage();
        return service.search(keyword).onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
            return Response.ok(viewPage).build();
        });
    }
    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        return getById(service, id, requestContext);
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid EmployeeDTO dto, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        return create(service, dto, requestContext);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, EmployeeDTO dto, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
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
