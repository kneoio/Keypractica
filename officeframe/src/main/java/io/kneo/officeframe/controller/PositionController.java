package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.service.PositionService;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

@ApplicationScoped
public class PositionController extends AbstractSecuredController<Position, PositionDTO> {

    @Inject
    PositionService service;

    public PositionController() {
        super(null);
    }

    public PositionController(UserService userService, PositionService service) {
        super(userService);
        this.service = service;
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/positions").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/positions/:id").handler(this::getOne);
        router.route(HttpMethod.POST, "/api/:org/positions/:id?").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/positions/:id").handler(this::delete);
    }

    private void get(RoutingContext rc) {
        getAll(service, rc);
    }

    private void getOne(RoutingContext rc) {
        getById(service, rc);
    }

    private void upsert(RoutingContext rc) {
        // Implementation for upsert
    }

    private void delete(RoutingContext rc) {
        // Implementation for delete
    }
}