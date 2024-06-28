package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.model.user.IUser;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.service.OrganizationService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.Optional;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/orgs")
public class OrganizationController extends AbstractSecuredController<Organization, OrganizationDTO> {

    @Inject
    OrganizationService service;

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "0"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));

        getAll(service, rc, page, size).subscribe().with(
                response -> rc.response()
                        .setStatusCode(response.getStatus())
                        .end(JsonObject.mapFrom(response.getEntity()).encode()),
                failure -> {
                    LOGGER.error("Error processing request: ", failure);
                    rc.response().setStatusCode(500).end("Internal Server Error");
                }
        );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) {
        String id = rc.pathParam("id");
        getById(service, id, rc).subscribe().with(
                response -> rc.response().setStatusCode(response.getStatus()).end(JsonObject.mapFrom(response.getEntity()).encode()),
                failure -> {
                    LOGGER.error(failure.getMessage(), failure);
                    rc.response().setStatusCode(500).end(failure.getMessage());
                }
        );
    }

    @Route(path = "/", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            OrganizationDTO dto = jsonObject.mapTo(OrganizationDTO.class);
            Optional<IUser> userOptional = getUserId(rc);

            if (userOptional.isPresent()) {
                service.add(dto, userOptional.get()).subscribe().with(
                        id -> rc.response().setStatusCode(201).putHeader("Location", "/api/" + rc.pathParam("org") + "/orgs/" + id).end(),
                        failure -> {
                            LOGGER.error(failure.getMessage(), failure);
                            rc.response().setStatusCode(500).end(failure.getMessage());
                        }
                );
            } else {
                rc.response().setStatusCode(403).end(String.format("%s is not allowed", getUserOIDCName(rc)));
            }
        } catch (DecodeException e) {
            LOGGER.error("Error decoding request body: {}", e.getMessage());
            rc.response().setStatusCode(400).end("Invalid request body");
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(RoutingContext rc) {
        String id = rc.pathParam("id");
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            OrganizationDTO dto = jsonObject.mapTo(OrganizationDTO.class);
            Optional<IUser> userOptional = getUserId(rc);

            if (userOptional.isPresent()) {
                service.update(id, dto, userOptional.get()).subscribe().with(
                        count -> rc.response().setStatusCode(count > 0 ? 200 : 404).end(),
                        failure -> {
                            LOGGER.error(failure.getMessage(), failure);
                            rc.response().setStatusCode(500).end(failure.getMessage());
                        }
                );
            } else {
                rc.response().setStatusCode(403).end(String.format("%s is not allowed", getUserOIDCName(rc)));
            }
        } catch (DecodeException e) {
            LOGGER.error("Error decoding request body: {}", e.getMessage());
            rc.response().setStatusCode(400).end("Invalid request body");
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) {
        String id = rc.pathParam("id");
        Optional<IUser> userOptional = getUserId(rc);

        if (userOptional.isPresent()) {
            service.delete(id, userOptional.get()).subscribe().with(
                    count -> rc.response().setStatusCode(count > 0 ? 200 : 404).end(),
                    failure -> {
                        LOGGER.error(failure.getMessage(), failure);
                        rc.response().setStatusCode(500).end(failure.getMessage());
                    }
            );
        } else {
            rc.response().setStatusCode(403).end(String.format("%s is not allowed", getUserOIDCName(rc)));
        }
    }

}