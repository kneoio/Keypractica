package com.semantyca.officeframe.controller;


import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.repository.exception.DocumentExistsException;
import com.semantyca.core.repository.exception.DocumentModificationAccessException;
import com.semantyca.officeframe.dto.OrganizationDTO;
import com.semantyca.officeframe.model.Organization;
import com.semantyca.officeframe.service.OrganizationService;
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

@Path("/orgs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganizationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class.getSimpleName());
    @Inject
    OrganizationService service;

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
        View<Organization> view = new View<>(service.getAll(100, 0, 1).await().indefinitely());
        viewPage.addPayload("view_data", view);
        return Response.ok(viewPage).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        Organization user = service.get(id);
        return Response.ok(user).build();
    }

    @POST
    @Path("/")
    public Response create(OrganizationDTO dto) throws DocumentExistsException {
        return Response.created(URI.create("/" + service.add(dto))).build();
    }

    @PUT
    @Path("/")
    public Response update(OrganizationDTO dto) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
