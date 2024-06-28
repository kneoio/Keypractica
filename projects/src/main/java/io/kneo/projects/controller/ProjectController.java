package io.kneo.projects.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.actions.Action;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.actions.cnst.ActionType;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.cnst.RunMode;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.actions.ProjectActionsFactory;
import io.kneo.projects.model.Project;
import io.kneo.projects.model.cnst.ProjectStatusType;
import io.kneo.projects.service.ProjectService;
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

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class ProjectController extends AbstractSecuredController<Project, ProjectDTO> {
    @Inject
    ProjectService service;

    @GET
    @Path("/")
    public Uni<Response> get(@Valid @Min(1) @QueryParam("page") int page, @Valid @Min(10) @QueryParam("size") int pageSize, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            Uni<Integer> countUni = service.getAllCount(user.getId());
            Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, pageSize));
            Uni<Integer> pageNumUni = Uni.createFrom().item(page);
            Uni<Integer> offsetUni = Uni.combine().all()
                    .unis(pageNumUni, Uni.createFrom().item(user.getPageSize()))
                    .asTuple().map(tuple -> RuntimeUtil.calcStartEntry(tuple.getItem1(), tuple.getItem2()));
            Uni<List<ProjectDTO>> prjsUni = offsetUni.onItem().transformToUni(offset -> service.getAll(pageSize, offset, user.getId()));
            return Uni.combine().all().unis(prjsUni, offsetUni, pageNumUni, countUni, maxPageUni).asTuple().map(tuple -> {
                List<ProjectDTO> prjs = tuple.getItem1();
                int offset = tuple.getItem2();
                int pageNum = tuple.getItem3();
                int count = tuple.getItem4();
                int maxPage = tuple.getItem5();

                ViewPage viewPage = new ViewPage();
                ActionBox actions = ProjectActionsFactory.getViewActions(user.getActivatedRoles());
                Action action = new Action();
                action.setIsOn(RunMode.ON);
                action.setCaption(ActionType.CLOSE.getAlias());
                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, List.of(action));
                if (pageNum == 0) pageNum = 1;
                View<ProjectDTO> dtoEntries = new View<>(prjs, count, pageNum, maxPage, pageSize);
                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                return Response.ok(viewPage).build();
            });
        } else {
            return Uni.createFrom()
                    .item(Response.status(Response.Status.FORBIDDEN)
                            .entity(String.format("%s is not allowed", AnonymousUser.USER_NAME))
                            .build());
        }
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
    @Path("/status/{status}")
    @JsonView(Views.ListView.class)
    public Uni<Response> searchByStatus(@PathParam("status") ProjectStatusType status) {
        if (status == null || status == ProjectStatusType.UNKNOWN) {
            throw new DataValidationException("Invalid status value.");
        }
        ViewPage viewPage = new ViewPage();
        return service.searchByStatus(status).onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
            return service.getDTO(id, user)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.DOC_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure(DocumentHasNotFoundException.class).recoverWithItem(this::postNotFoundError)
                    .onFailure().recoverWithItem(this::postError);
        } else {
            return Uni.createFrom().item(
                    Response.ok(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }



    @POST
    @Path("/")
    public Uni<Response> create(ProjectDTO dto, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.add(dto, userOptional.get())
                    .onItem().transform(createdProject -> Response.ok(createdProject).build());
        } else {
            return Uni.createFrom().item(Response.ok(String.format("User %s does not exist", getUserOIDCName(requestContext))).build());
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "updateProject")
    public Uni<Response> update(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Valid ProjectDTO dto, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        return update(id, service, dto, requestContext);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.noContent().build();
    }
}
