
package com.semantyca.core.controller;

import com.semantyca.core.dto.Workspace;
import com.semantyca.core.service.LanguageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/workspace")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceController {

    @Inject
    LanguageService service;

    @GET
    @Path("/")
    public Response get() {
        return Response.ok(new Workspace(service)).build();
    }
}
