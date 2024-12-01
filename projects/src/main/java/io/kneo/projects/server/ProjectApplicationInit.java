package io.kneo.projects.server;

import io.kneo.core.controller.LanguageController;
import io.kneo.core.server.ApplicationInit;
import io.kneo.officeframe.controller.LabelController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProjectApplicationInit extends ApplicationInit {

    @Inject
    LanguageController languageController;

    @Inject
    LabelController labelController;

    @Override
    protected void setupRoutes() {
        super.setupRoutes();
        languageController.setupRoutes(router);
        labelController.setupRoutes(router);
        logRegisteredRoutes();
    }
}