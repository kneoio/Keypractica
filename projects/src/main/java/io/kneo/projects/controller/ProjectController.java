package io.kneo.projects.controller;

import io.kneo.core.dto.actions.ActionBar;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewOptionsFactory;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.repository.exception.DocumentExistsException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.actions.ProjectActionsFactory;
import io.kneo.projects.service.ProjectService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
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
import java.util.List;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectController {
    @Inject
    ProjectService service;
    @GET
    @Path("/")
    @RolesAllowed("dev")
    public Uni<Response> get(@BeanParam Parameters parameters, @Context ContainerRequestContext requestContext) {
        IUser user = new SuperUser();
        Uni<Integer> countUni = service.getAllCount(user.getId());
        Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
        Uni<Integer> pageNumUni = Uni.createFrom().item(parameters.page);
        Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
        Uni<List<ProjectDTO>> prjsUni = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset, user.getId()));

        return Uni.combine().all().unis(prjsUni, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((prjs, offset, pageNum, count, maxPage) -> {
            ViewPage viewPage = new ViewPage();
            viewPage.addPayload(PayloadType.ACTIONS, ProjectActionsFactory.getViewActions());
            viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
            if (pageNum == 0) pageNum = 1;
            View<ProjectDTO> dtoEntries = new View<>(prjs, count, pageNum, maxPage, user.getPageSize());
            viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id, @Context ContainerRequestContext requestContext) {
        IUser user = (IUser) requestContext.getProperty("user");
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());

        return service.get(id, user.getId())
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }


    @POST
    @Path("/")
    public Response create(ProjectDTO dto) throws DocumentExistsException {
        return Response.created(URI.create("/" + service.add(dto))).build();
    }

    @PUT
    @Path("/")
    public Response update(LanguageDTO dto) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

    static class Parameters {
        @QueryParam("page")
        int page;
    }

}
