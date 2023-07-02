
package com.semantyca.projects.controller;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.dto.view.ViewPage;
import com.semantyca.model.Language;
import com.semantyca.projects.service.ProjectService;
import com.semantyca.repository.exception.DocumentExistsException;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class.getSimpleName());
    @Inject
    ProjectService service;

    @GET
    @Path("/")
    public Response get()  {
        return Response.ok(new ViewPage(service.getAll())).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        Language user = service.get(id);
        return Response.ok(user).build();
    }

    @POST
    @Path("/")
    public Response create(LanguageDTO dto) throws DocumentExistsException {
        return Response.created(URI.create("/" + service.add(dto))).build();
    }

    @PUT
    @Path("/")
    public Response update(LanguageDTO dto) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(dto).getIdentifier())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
