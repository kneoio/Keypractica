package io.kneo.kneobroadcaster.server;

import io.kneo.core.server.ApplicationInit;
import io.kneo.kneobroadcaster.controller.SoundFragmentController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KneoBroadcasterApplicationInit extends ApplicationInit {

    @Inject
    SoundFragmentController soundFragmentController;

    @Override
    protected void setupRoutes() {
        super.setupRoutes();
        soundFragmentController.setupRoutes(router);
        logRegisteredRoutes();
    }
}