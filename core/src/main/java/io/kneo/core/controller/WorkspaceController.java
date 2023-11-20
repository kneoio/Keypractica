
package io.kneo.core.controller;

import io.kneo.core.dto.Workspace;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.User;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.ModuleService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceController {
    @Inject
    private LanguageService languageService;
    @Inject
    private ModuleService moduleService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/")
    public Response getDefault(@Context ContainerRequestContext requestContext) {
        return get(requestContext);
    }

    @GET
    @Path("/workspace")
    @PermitAll
    //@Authenticated
    public Response get(@Context ContainerRequestContext requestContext) {
       String accessToken = jwt.getSubject();
        String currentUser = securityIdentity.getPrincipal().getName();
        if (currentUser != null && currentUser.equals(AnonymousUser.ID)) {
            return Response.ok(new Workspace((IUser) new User.Builder().setLogin(currentUser), languageService)).build();
        } else {
            return Response.ok("new Workspace(new AnonymousUser(), languageService, moduleService)").build();
        }
    }
}
