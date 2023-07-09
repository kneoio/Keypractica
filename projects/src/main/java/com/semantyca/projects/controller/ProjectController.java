package com.semantyca.projects.controller;

import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.repository.exception.DocumentExistsException;
import com.semantyca.core.repository.exception.DocumentModificationAccessException;
import com.semantyca.projects.actions.ProjectActionsFactory;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.service.ProjectService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.Set;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectController {
    @Inject
    ProjectService service;

    @Inject
    JsonWebToken jwt;
    @GET
    @Path("/")
    @PermitAll
    public Response get()  {
        String userName = jwt.getName();
        Set<String> userGroups = jwt.getGroups();
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.ACTIONS, ProjectActionsFactory.getViewActions());
        viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
        View<ProjectDTO> view = new View<>(service.getAll(100, 0, 1).await().indefinitely());
        viewPage.addPayload(PayloadType.VIEW_DATA, view);
        return Response.ok(viewPage).build();
    }

/*    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        FormPage page = new FormPage();
        page.addPayload("form_actions", new ActionBar());

        return service.get(id)
                .onItem().transform(p -> {
                    page.addPayload("form_data", p);
                    return Response.ok(page).build();
                })
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }*/


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

}
