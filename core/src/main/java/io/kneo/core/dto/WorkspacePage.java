package io.kneo.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.dto.view.View;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.server.EnvConst;
import io.kneo.core.server.Environment;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.ModuleService;
import io.smallrye.mutiny.Uni;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkspacePage extends AbstractPage {

    public WorkspacePage(IUser user, LanguageService languageService, ModuleService moduleService) {
        addPayload("application_name", String.format("%s %s", EnvConst.APP_ID, EnvConst.VERSION));
        addPayload("user", user.getUserName());
        addPayload("redirect", "projects");
        Uni<List<LanguageDTO>> languageListUni = languageService.getAll(100, 0);
        Uni<List<ModuleDTO>> moduleServiceAll;
        if (user.getId() == AnonymousUser.ID) {
            moduleServiceAll = moduleService.findAll(Environment.publicModules);
        } else {
            moduleServiceAll = moduleService.getAll(100, 0);
        }
        addPayload("available_languages", new View<>(languageListUni.await().indefinitely()));
        addPayload("available_modules", new View<>(moduleServiceAll.await().indefinitely()));
    }

    public WorkspacePage(IUser user, LanguageService service) {
        addPayload("application_name", String.format("%s %s", EnvConst.APP_ID, EnvConst.VERSION));
        addPayload("user", user.getUserName());
        addPayload("redirect", "projects");
        Uni<List<LanguageDTO>> languageListUni = service.getAll(100, 0);
        addPayload("available_languages", new View<>(languageListUni.await().indefinitely()));
    }

}
