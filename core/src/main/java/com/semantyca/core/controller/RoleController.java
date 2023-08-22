package com.semantyca.core.controller;

import com.semantyca.core.dto.RoleDTO;
import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.form.FormPage;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.repository.exception.DocumentExistsException;
import com.semantyca.core.repository.exception.DocumentModificationAccessException;
import com.semantyca.core.service.RoleService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleController {
    @Inject
    RoleService service;

    @GET
    @Path("/")
    public Uni<Response> get()  {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.ACTIONS, ViewOptionsFactory.getProjectOptions());
        return service.getAll(100, 0).onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        return service.get(id)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/")
    public Response create(RoleDTO dto) throws DocumentExistsException {
        return Response.created(URI.create("/" + service.add(dto))).build();
    }

    @PUT
    @Path("/")
    public Response update(RoleDTO dto) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
