package io.kneo.core.controller;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.model.Language;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.UserService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/languages")
public class LanguageController extends AbstractSecuredController<Language, LanguageDTO> {

    LanguageService service;

    @Inject
    public LanguageController(UserService userService, LanguageService service) {
        super(userService);
        this.service = service;
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) {
        getAll(service, rc);
    }

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getOne(RoutingContext rc) throws UserNotFoundException {
        getById(service, rc);
    }

    @Route(path = "/:id?", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void upsert(RoutingContext rc) throws UserNotFoundException {
        // Implementation for upsert operation
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) throws UserNotFoundException {
        // Implementation for delete operation
    }
}