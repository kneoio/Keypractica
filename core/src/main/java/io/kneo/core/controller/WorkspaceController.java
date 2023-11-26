
package io.kneo.core.controller;

import io.kneo.core.dto.Workspace;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.User;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.ModuleService;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
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
public class WorkspaceController extends AbstractController {

    @Inject
    private LanguageService languageService;
    @Inject
    private ModuleService moduleService;

    @GET
    @Path("/")
    public Response getDefault(@Context ContainerRequestContext requestContext) {
        return get(requestContext);
    }

    @GET
    @Path("/workspace")
    //@PermitAll
    public Response get(@Context ContainerRequestContext requestContext) {
        DefaultJWTCallerPrincipal securityIdentity = (DefaultJWTCallerPrincipal) requestContext.getSecurityContext().getUserPrincipal();
        if (securityIdentity != null) {
            IUser currentUser = new User.Builder().setLogin(getUserName(securityIdentity)).build();
            if (isSupervisor(securityIdentity)) {
                return Response.ok(new Workspace(currentUser, languageService, moduleService)).build();
            } else {
                return Response.ok(new Workspace(currentUser, languageService)).build();
            }
        } else {
            return Response.ok(new Workspace(AnonymousUser.build(), languageService)).build();
        }
    }
}
