package com.semantyca.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.model.Module;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.server.EnvConst;
import com.semantyca.core.service.LanguageService;
import com.semantyca.core.service.ModuleService;
import io.smallrye.mutiny.Uni;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace extends AbstractPage {

    public Workspace(IUser user, LanguageService languageService, ModuleService moduleService) {
        addPayload("application_name", String.format("%s %s", EnvConst.APP_ID, EnvConst.VERSION));
        addPayload("user", user.getId());
        addPayload("redirect", "projects");
        Uni<List<LanguageDTO>> languageListUni = languageService.getAll(100, 0);
        Uni<List<Module>> moduleServiceAll = moduleService.getAll(100, 0);
        addPayload("available_languages", new View(languageListUni.await().indefinitely()));
        addPayload("available_modules", new View(moduleServiceAll.await().indefinitely()));
    }

    public Workspace(IUser user, LanguageService service) {
        addPayload("application_name", String.format("%s %s", EnvConst.APP_ID, EnvConst.VERSION));
        addPayload("user", user.getId());
        addPayload("redirect", "projects");
        Uni<List<LanguageDTO>> languageListUni = service.getAll(100, 0);
        addPayload("available_languages", new View(languageListUni.await().indefinitely()));
    }

}
