package io.kneo.officeframe.controller;


import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
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
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page);
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
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
            return service.getDTO(id, user)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.FORM_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(t -> {
                        LOGGER.error(t.getMessage(), t);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    });
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid EmployeeDTO dto, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        return create(service, dto, requestContext);
    }

    @PUT
    @Path("/")
    public Uni<Response> update(EmployeeDTO dto, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        return update(service, dto, requestContext);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
