package io.kneo.core.controller;

import io.kneo.core.dto.WorkspacePage;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.dto.document.UserModuleDTO;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.UserService;
import io.kneo.core.service.WorkspaceService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;

@RouteBase(path = "/api/:org/workspace")
public class WorkspaceController extends AbstractSecuredController<Module, ModuleDTO> {

    @Inject
    private WorkspaceService workspaceService;
    @Inject
    private LanguageService languageService;

    public WorkspaceController(UserService userService) {
        super(userService);
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public Uni<Response> get(RoutingContext rc) {
        String acceptLanguage = rc.request().getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (acceptLanguage != null) {
            Locale preferredLocale = Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());
        }
        IUser user = getUserId(rc);
        Uni<List<UserModuleDTO>> moduleUnis = workspaceService.getAvailableModules(user);
        Uni<List<LanguageDTO>> languageUnis = workspaceService.getAvailableLanguages();

        return Uni.combine().all().unis(moduleUnis, languageUnis).with((modules, languages) -> {
            WorkspacePage page = new WorkspacePage(user, languages, modules);
            return Response.ok(page).build();
        }).onFailure().recoverWithItem(
                this::postError);
    }
}
