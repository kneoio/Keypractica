package io.kneo.projects.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.actions.ProjectActionsFactory;
import io.kneo.projects.model.Project;
import io.kneo.projects.model.cnst.ProjectStatusType;
import io.kneo.projects.service.ProjectService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import java.net.URI;
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
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            Uni<Integer> countUni = service.getAllCount(user.getId());
            Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
            Uni<Integer> pageNumUni = Uni.createFrom().item(page);
            Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
            Uni<List<ProjectDTO>> prjsUni = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset, user.getId()));
            return Uni.combine().all().unis(prjsUni, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((prjs, offset, pageNum, count, maxPage) -> {
                ViewPage viewPage = new ViewPage();
                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ProjectActionsFactory.getViewActions());
                if (pageNum == 0) pageNum = 1;
                View<ProjectDTO> dtoEntries = new View<>(prjs, count, pageNum, maxPage, user.getPageSize());
                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                return Response.ok(viewPage).build();
            });
        } else {
            return Uni.createFrom().item(Response.ok(String.format("user %s is not exists", "ddd")).build());
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
    @Tag(name = "ai")
    @Path("/status/{status}")
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
    public Uni<Response> getById(@Pattern(regexp = UUID_PATTERN) @PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
            return service.get(id, user.getId())
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.DOC_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        } else {
            return Uni.createFrom().item(Response.ok(String.format("user %s is not exists", getUserOIDCName(requestContext))).build());
        }
    }

    @POST
    @Path("/")
    public Uni<Response> create(ProjectDTO dto) {
        return service.add(dto)
                .onItem().transform(createdProject -> Response.ok(createdProject).build());
    }


    @PUT
    @Path("/")
    public Response update(LanguageDTO dto) {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.noContent().build();
    }
}
