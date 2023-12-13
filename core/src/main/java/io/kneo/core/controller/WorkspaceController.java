
package io.kneo.core.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.WorkspacePage;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.dto.document.UserModuleDTO;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.AnonymousUser;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;
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
    @JsonView(Views.ListView.class)
    public Uni<Response> get(@Context ContainerRequestContext requestContext) {
        String acceptLanguage = requestContext.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
        if (acceptLanguage != null) {
            Locale preferredLocale = Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());
        }
        Optional<IUser> userOptional = getUserId(requestContext);
        IUser user = userOptional.orElseGet(AnonymousUser::build);
        Uni<List<UserModuleDTO>> moduleUnis = workspaceService.getAvailableModules(user);
        Uni<List<LanguageDTO>> languageUnis = workspaceService.getAvailableLanguages();

        return Uni.combine().all().unis(moduleUnis, languageUnis)
                .combinedWith((modules, languages) -> {
                    WorkspacePage page = new WorkspacePage(user, languages, modules);
                    return Response.ok(page).build();
                })
                .onFailure().recoverWithItem(
                        this::postError);
    }



}
