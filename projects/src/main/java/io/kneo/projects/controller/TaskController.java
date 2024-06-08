package io.kneo.projects.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.dto.actions.TaskActionsFactory;
import io.kneo.projects.model.Task;
import io.kneo.projects.model.cnst.TaskStatus;
import io.kneo.projects.service.TaskService;
import io.smallrye.mutiny.Uni;
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
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;
import java.util.Optional;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public final class TaskController extends AbstractSecuredController<Task, TaskDTO> {
    @Inject
    TaskService service;

    @GET
    @Path("/")
    @Operation(operationId = "getAllTasks")
    public Uni<Response> getAll(@Valid @Min(0) @QueryParam("page") int page, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            Uni<Integer> countUni = service.getAllCount(user.getId());
            Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
            Uni<Integer> pageNumUni = Uni.createFrom().item(page);
            Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
            Uni<List<TaskDTO>> unis = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset, user.getId()));

            return Uni.combine().all().unis(unis, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((tasks, offset, pageNum, count, maxPage) -> {
                ViewPage viewPage = new ViewPage();
                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, TaskActionsFactory.getViewActions(user.getActivatedRoles()));
                if (pageNum == 0) pageNum = 1;
                View<TaskDTO> dtoEntries = new View<>(tasks, count, pageNum, maxPage, user.getPageSize());
                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                return Response.ok(viewPage).build();
            });
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @GET
    @Path("/status/{status}")
    @Operation(operationId = "searchTasksByStatus")
    @JsonView(Views.ListView.class)
    public Uni<Response> searchByStatus(@PathParam("status") TaskStatus status) {
        ViewPage viewPage = new ViewPage();
        return service.searchByStatus(status).onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
            return Response.ok(viewPage).build();
        });
    }


    @GET
    @Path("/{id}")
    @Operation(operationId = "getTaskById")
    public Uni<Response> get(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
            return service.get(id, user)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.DOC_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(this::postNotFoundError)
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @GET
    @Path("/template")
    @Operation(operationId = "getTaskTemplate")
    public Uni<Response> getTemplate(@Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
            return service.getTemplate(user)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.TEMPLATE, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @POST
    @Path("/")
    @Operation(operationId = "createTask", summary = "Create task")
    public Uni<Response> create(@Valid TaskDTO dto, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.add(dto, userOptional.get())
                    .onItem().transform(id -> Response.status(Response.Status.CREATED).build());
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "updateTask")
    public Uni<Response> update(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Valid TaskDTO dto, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        return update(id, service, dto, requestContext);
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deleteTaskById")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@PathParam("id") String uuid, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException {
        return delete(uuid, service, requestContext);
    }

}
