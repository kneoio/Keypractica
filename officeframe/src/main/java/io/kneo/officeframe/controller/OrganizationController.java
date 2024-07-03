package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.service.OrganizationService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/orgs")
public class OrganizationController extends AbstractSecuredController<Organization, OrganizationDTO> {

    OrganizationService service;

    @Inject
    public OrganizationController(UserService userService, OrganizationService service) {
        super(userService);
        this.service = service;
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "0"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));
        service.getAll(size, page)
                .subscribe().with(
                        dtos -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(dtos).encode()),
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) throws UserNotFoundException {
        service.getDTO(rc.pathParam("id"), getUser(rc), LanguageCode.valueOf(rc.acceptableLanguages().getFirst().value()))
                .subscribe().with(
                        dto -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(dto).encode()),
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void upsert(RoutingContext rc) throws UserNotFoundException {
        JsonObject jsonObject = rc.body().asJsonObject();
        OrganizationDTO dto = jsonObject.mapTo(OrganizationDTO.class);
        service.upsert(rc.pathParam("id"), dto, getUser(rc))
                .subscribe().with(
                        id -> rc.response().setStatusCode(200).end(id.toString()),
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) throws UserNotFoundException {
        service.delete(rc.pathParam("id"), getUser(rc))
                .subscribe().with(
                        count -> {
                            if (count > 0) {
                                rc.response().setStatusCode(200).end();
                            } else {
                                rc.fail(404);
                            }
                        },
                        rc::fail
                );
    }
}