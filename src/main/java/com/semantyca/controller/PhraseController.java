package com.semantyca.controller;

import com.semantyca.dto.PhraseDTO;
import com.semantyca.dto.document.DocumentOutcome;
import com.semantyca.dto.view.ViewPage;
import com.semantyca.repository.exception.DocumentExists;
import com.semantyca.repository.exception.DocumentModificationAccessException;
import com.semantyca.service.PhraseService;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;


@Singleton
@Path("/phrases")
public class PhraseController {

    @Inject
    JsonWebToken jwt;
    @Inject
    PhraseService phraseService;

    @GET
    @RolesAllowed({ "User", "Admin" })
    @Path("/")
    public Response get()  {
        return Response.ok(new ViewPage(phraseService.getAll())).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id)  {
        return Response.ok(phraseService.getById(id)).build();
    }

    @POST
    @Path("/")
    public Response create(PhraseDTO phraseDTO) throws DocumentExists, URISyntaxException {
        return Response.created(new URI("phrases/" + phraseService.add(phraseDTO).getId().toString())).build();
    }

    @PUT
    @Path("/")
    public Response update(PhraseDTO sentence) throws DocumentModificationAccessException {
        DocumentOutcome outcome = new DocumentOutcome();
        outcome.addPayload(phraseService.update(sentence));
        return Response.ok().entity(outcome).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWord(@PathParam("id") String id) {
        return Response.ok().entity(phraseService.delete(id)).build();
    }

}
