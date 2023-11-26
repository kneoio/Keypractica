
package io.kneo.core.controller;

import io.kneo.core.dto.Workspace;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.ModuleService;
import jakarta.annotation.security.RolesAllowed;
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
@RolesAllowed("**")
public class UtilityController {
    @Inject
    private LanguageService languageService;
    @Inject
    private ModuleService moduleService;
    @GET
    @Path("/translations")
    public Response get(@Context ContainerRequestContext requestContext) {
        IUser currentUser = (IUser) requestContext.getProperty("user");
        if (currentUser.getId() == AnonymousUser.ID) {
            return Response.ok(new Workspace(currentUser, languageService)).build();
        } else {
            return Response.ok(new Workspace(currentUser, languageService, moduleService)).build();
        }
    }
}
