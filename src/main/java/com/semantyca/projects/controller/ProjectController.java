package com.semantyca.projects.controller;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.dto.view.View;
import com.semantyca.dto.view.ViewOptionsFactory;
import com.semantyca.dto.view.ViewPage;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.service.ProjectService;
import com.semantyca.repository.exception.DocumentExistsException;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class.getSimpleName());
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
        viewPage.addPayload("view_options", ViewOptionsFactory.getProjectOptions());
        View<Project> view = new View<>(service.getAll(100, 0, 1).await().indefinitely());
        viewPage.addPayload("view_data", view);
        return Response.ok(viewPage).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        Project user = service.get(id);
        return Response.ok(user).build();
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

}
