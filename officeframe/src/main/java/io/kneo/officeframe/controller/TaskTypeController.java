package io.kneo.officeframe.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.dto.TaskTypeDTO;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.service.TaskTypeService;
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

@Path("/tasktypes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class TaskTypeController extends AbstractSecuredController<TaskType, TaskTypeDTO> {
    @Inject
    TaskTypeService service;

    @GET
    @Path("/")
    @JsonView(Views.ListView.class)
    public Uni<Response> getAll(@Valid @Min(0) @QueryParam("page") int page, @Context ContainerRequestContext requestContext) {
        return getAll(service, requestContext, page);
    }

    @GET
    @Path("/{identifier}")
    public Uni<Response> get(String identifier) {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
        return service.findByIdentifier(identifier)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().recoverWithItem(this::postError);
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid LabelDTO dto) {
        return service.add(dto)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @PUT
    @Path("/")
    public Response update(@Valid LabelDTO dto) {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
