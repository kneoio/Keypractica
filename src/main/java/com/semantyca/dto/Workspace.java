package com.semantyca.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.dto.view.View;
import com.semantyca.server.EnvConst;
import com.semantyca.service.LanguageService;
import io.smallrye.mutiny.Uni;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace extends AbstractPage {

    public Workspace(LanguageService service) {
        addPayload("application_name", String.format("%s %s", EnvConst.APP_ID, EnvConst.VERSION));
        addPayload("redirect", "projects");
        Uni<List<LanguageDTO>> languageListUni = service.getAll(100, 0);
        addPayload("available_languages", new View(languageListUni.await().indefinitely()));
    }

}
