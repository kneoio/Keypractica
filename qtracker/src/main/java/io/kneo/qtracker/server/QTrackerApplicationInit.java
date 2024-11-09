package io.kneo.qtracker.server;

import io.kneo.core.server.ApplicationInit;
import io.kneo.qtracker.controller.ConsumingController;
import io.kneo.qtracker.controller.OwnerController;
import io.kneo.qtracker.controller.VehicleController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class QTrackerApplicationInit extends ApplicationInit {

    @Inject
    ConsumingController consumingController;

    @Inject
    OwnerController ownerController;

    @Inject
    VehicleController vehicleController;

    @Override
    protected void setupRoutes() {
        super.setupRoutes();
        consumingController.setupRoutes(router);
        ownerController.setupRoutes(router);
        vehicleController.setupRoutes(router);
        logRegisteredRoutes();
    }
}