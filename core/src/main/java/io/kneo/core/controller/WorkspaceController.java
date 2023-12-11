
package io.kneo.core.controller;

import io.kneo.core.dto.WorkspacePage;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.WorkspaceService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceController extends AbstractSecuredController<Module, ModuleDTO> {
    @Inject
    private WorkspaceService workspaceService;
    @Inject
    private LanguageService languageService;

    @GET
    @Path("/")
    public Uni<Response> getDefault(@Context ContainerRequestContext requestContext) {
        return get(requestContext);
    }

    @GET
    @Path("/workspace")
    public Uni<Response> get(@Context ContainerRequestContext requestContext) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            Uni<List<ModuleDTO>> moduleUnis = workspaceService.getAvailableModules(user);
            Uni<List<LanguageDTO>> languageUnis = languageService.getAll(0, 0);

            return Uni.combine().all().unis(moduleUnis, languageUnis)
                    .combinedWith((modules, languages) -> {
                        WorkspacePage page = new WorkspacePage(user, languages, modules);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(
                            throwable -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }



}
