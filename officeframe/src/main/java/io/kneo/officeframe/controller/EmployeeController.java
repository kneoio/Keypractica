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
import jakarta.json.JsonObject;
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
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
            return service.getDTO(id, user)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.DOC_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(this::postNotFoundError)
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).entity(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid EmployeeDTO dto, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.add(dto, userOptional.get())
                    .onItem().transform(createdEmployee -> Response.ok(createdEmployee).build())
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).entity(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, EmployeeDTO dto, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.update(id, dto, userOptional.get())
                    .onItem().transform(updatedCount -> Response.ok(updatedCount).build())
                    .onFailure().recoverWithItem(this::postNotFoundError)
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).entity(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> patch(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id,
                               JsonObject updates,
                               @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.patch(id, updates, userOptional.get())
                    .onItem().transform(count -> Response.ok(count).build())
                    .onFailure().recoverWithItem(this::postNotFoundError)
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN)
                    .entity(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.delete(id, userOptional.get())
                    .onItem().transform(count -> Response.ok(count).build())
                    .onFailure().recoverWithItem(this::postNotFoundError)
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.FORBIDDEN).entity(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }
}
