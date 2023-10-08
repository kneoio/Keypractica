
package com.semantyca.core.controller;

import com.semantyca.core.dto.Workspace;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.service.LanguageService;
import com.semantyca.core.service.ModuleService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceController {
    @Inject
    private LanguageService languageService;
    @Inject
    private ModuleService moduleService;
    @GET
    @Path("/workspace")
    public Response get(@Context ContainerRequestContext requestContext) {
        IUser currentUser = (IUser) requestContext.getProperty("user");
        if (currentUser.getId() == AnonymousUser.ID) {
            return Response.ok(new Workspace(currentUser, languageService)).build();
        } else {
            return Response.ok(new Workspace(currentUser, languageService, moduleService)).build();
        }
    }
}
