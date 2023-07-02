
package com.semantyca.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.semantyca.dto.Workspace;
import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.model.Language;
import com.semantyca.projects.controller.ProjectController;
import com.semantyca.repository.exception.DocumentExistsException;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import com.semantyca.service.LanguageService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Path("/workspace")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class.getSimpleName());

    @Inject
    LanguageService service;

    @GET
    @Path("/")
    public Response get()  {
        try {
            return Response.ok(new Workspace(service)).build();
        } catch (JsonProcessingException e) {
            LOGGER.warn(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        Language user = service.get(id);
        return Response.ok(user).build();
    }

    @POST
    @Path("/")
    @RolesAllowed({"supervisor","admin"})
    public Response create(LanguageDTO dto) {
        try {
            return Response.created(URI.create("/" + service.add(dto))).build();
        } catch (DocumentExistsException e) {
            LOGGER.warn(e.getDetailedMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/")
    public Response update(LanguageDTO dto) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(dto).getIdentifier())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWord(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
