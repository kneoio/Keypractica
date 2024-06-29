package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.model.user.IUser;
import io.kneo.officeframe.dto.OrgCategoryDTO;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.service.OrgCategoryService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.Optional;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/orgcategories")
public class OrgCategoryController extends AbstractSecuredController<OrgCategory, OrgCategoryDTO> {

    @Inject
    OrgCategoryService service;

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
        getById(service, id, rc);
    }

  /*  @Route(path = "/", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        JsonObject jsonObject = rc.body().asJsonObject();
        OrgCategoryDTO dto = jsonObject.mapTo(OrgCategoryDTO.class);
        create(service, dto, rc).subscribe().with(
                response -> rc.response()
                        .setStatusCode(response.getStatus())
                        .putHeader("Location", response.getHeaderString("Location"))
                        .end(),
                failure -> {
                    LOGGER.error("Error processing request: ", failure);
                    rc.response().setStatusCode(500).end("Internal Server Error");
                }
        );
    }*/

   /* @Route(path = "/:id", methods = Route.HttpMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(RoutingContext rc) {
        String id = rc.pathParam("id");
        JsonObject jsonObject = rc.body().asJsonObject();
        OrgCategoryDTO dto = jsonObject.mapTo(OrgCategoryDTO.class);
        try {
            update(id, service, dto, rc).subscribe().with(
                    response -> rc.response()
                            .setStatusCode(response.getStatus())
                            .end(),
                    failure -> {
                        LOGGER.error("Error processing request: ", failure);
                        rc.response().setStatusCode(500).end("Internal Server Error");
                    }
            );
        } catch (DocumentModificationAccessException e) {
            rc.response().setStatusCode(403).end("Access denied");
        }
    }*/

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) {
        String id = rc.pathParam("id");
        Optional<IUser> userOptional = getUserId(rc);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            service.delete(id, user).subscribe().with(
                    count -> rc.response().setStatusCode(count > 0 ? 200 : 404).end(),
                    failure -> {
                        LOGGER.error("Error processing request: ", failure);
                        rc.response().setStatusCode(500).end("Internal Server Error");
                    }
            );
        } else {
            rc.response().setStatusCode(401).end("User not found");
        }
    }
}