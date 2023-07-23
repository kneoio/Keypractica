package com.semantyca.projects.controller;

import com.semantyca.core.controller.AbstractSecuredController;
import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.form.FormPage;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.repository.exception.DocumentExistsException;
import com.semantyca.core.repository.exception.DocumentModificationAccessException;
import com.semantyca.core.util.RuntimeUtil;
import com.semantyca.projects.actions.ProjectActionsFactory;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.dto.TaskDTO;
import com.semantyca.projects.service.TaskService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

import static com.semantyca.core.util.RuntimeUtil.countMaxPage;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskController extends AbstractSecuredController<TaskDTO> {
    @Inject
    TaskService service;
    @GET
    @Path("/")
    public Uni<Response> getAll(@BeanParam Parameters params, @Context ContainerRequestContext requestContext)  {
        IUser user = (IUser) requestContext.getProperty("user");
        Uni<Integer> countUni = service.getAllCount(user.getId());
        Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
        Uni<Integer> pageNumUni = Uni.createFrom().item(params.page);
        Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
        Uni<List<TaskDTO>> prjsUni = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset, user.getId()));

        return Uni.combine().all().unis(prjsUni, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((prjs, offset, pageNum, count, maxPage) -> {
            ViewPage viewPage = new ViewPage();
            viewPage.addPayload(PayloadType.ACTIONS, ProjectActionsFactory.getViewActions());
            viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
            if (pageNum == 0) pageNum = 1;
            View dtoEntries = new View<>(prjs, count, pageNum, maxPage, user.getPageSize());
            viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") String id, @Context ContainerRequestContext requestContext)  {
        IUser user = (IUser) requestContext.getProperty("user");
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());

        return service.get(id, user.getId())
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().invoke(failure -> System.out.println("Failure: "  + failure.getMessage()))
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "gfdgfdgfdgfd").build());
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
