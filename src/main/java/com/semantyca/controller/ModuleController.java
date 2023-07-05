
package com.semantyca.controller;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.dto.view.View;
import com.semantyca.dto.view.ViewOptionsFactory;
import com.semantyca.dto.view.ViewPage;
import com.semantyca.model.Language;
import com.semantyca.model.Module;
import com.semantyca.projects.controller.ProjectController;
import com.semantyca.repository.exception.DocumentExistsException;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import com.semantyca.service.ModuleService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

@Path("/modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModuleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectController.class.getSimpleName());

    @Inject
    ModuleService service;

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
        View<Module> view = new View<>(service.getAll().await().indefinitely());
        viewPage.addPayload("view_data", view);
        return Response.ok(viewPage).build();
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
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWord(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
