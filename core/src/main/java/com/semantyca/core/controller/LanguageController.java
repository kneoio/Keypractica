
package com.semantyca.core.controller;

import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.form.FormPage;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.repository.exception.DocumentExistsException;
import com.semantyca.core.repository.exception.DocumentModificationAccessException;
import com.semantyca.core.service.LanguageService;
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

@Path("/languages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LanguageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageController.class.getSimpleName());

    @Inject
    LanguageService service;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/")
    @PermitAll
    public Response get()  {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
        View<LanguageDTO> view = new View<>(service.getAll(100, 0).await().indefinitely());
        viewPage.addPayload(PayloadType.VIEW_DATA, view);
        return Response.ok(viewPage).build();
    }

    @GET
    @Path("/code/{code}")
    public Response getByCode(@PathParam("code") String code)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        page.addPayload(PayloadType.FORM_DATA, service.findByCode(code.toUpperCase()).await().indefinitely());
        return Response.ok(page).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        page.addPayload(PayloadType.FORM_DATA, service.get(id).await().indefinitely());
        return Response.ok(page).build();
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
