package io.kneo.officeframe.controller;


import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewOptionsFactory;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.repository.exception.DocumentExistsException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.service.OrganizationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/orgs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganizationController {
    @Inject
    OrganizationService service;
    @GET
    @Path("/")
    public Response get()  {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
        View<OrganizationDTO> view = new View<>(service.getAll(100, 0).await().indefinitely());
        viewPage.addPayload(PayloadType.VIEW_DATA, view);
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
