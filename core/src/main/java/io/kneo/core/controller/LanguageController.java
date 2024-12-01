package io.kneo.core.controller;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.model.Language;
import io.kneo.core.service.LanguageService;
import io.kneo.core.service.UserService;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LanguageController extends AbstractSecuredController<Language, LanguageDTO> {

    @Inject
    LanguageService service;

    public LanguageController() {
        super(null);
    }

    public LanguageController(UserService userService, LanguageService service) {
        super(userService);
        this.service = service;
    }

    public void setupRoutes(Router router) {
        router.route( "/*").handler(this::addHeaders);
        router.route(HttpMethod.GET, "/api/:org/languages").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/languages/:id").handler(this::getOne);
        router.route(HttpMethod.POST, "/api/:org/languages/:id?").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/languages/:id").handler(this::delete);
    }

    private void get(RoutingContext rc) {
        getAll(service, rc);
    }

    private void getOne(RoutingContext rc) {
            getById(service, rc);
    }

    private void upsert(RoutingContext rc) {
        // Implementation for upsert operation
    }

    private void delete(RoutingContext rc) {
        // Implementation for delete operation
    }

}