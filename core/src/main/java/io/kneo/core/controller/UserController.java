package io.kneo.core.controller;

import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.User;
import io.kneo.core.service.UserService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/users")
public class UserController extends AbstractController<User, UserDTO> {

    @Inject
    UserService service;

    @Route(path = "/", methods = Route.HttpMethod.GET, produces = "application/json")
    public Uni<Response> get(RoutingContext rc) {
        Object org = rc.get("database");
        System.out.println(org);
        ViewPage viewPage = new ViewPage();
          return service.getAll().onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
                return Response.ok(viewPage).build();
        });
    }

 /*   @Route(path = "/api/:org/users/search/:keyword", methods = Route.HttpMethod.GET, produces = "application/json")
    public Uni<Response> search(@PathParam("keyword") String keyword) {
        return service.search(keyword)
                .onItem().transform(userList -> {
                    ViewPage viewPage = new ViewPage();
                    viewPage.addPayload(PayloadType.VIEW_DATA, userList);
                    return Response.ok(viewPage).build();
                });
    }*/


    /*@Route(path = "/api/:org/users/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public Uni<Response> getById(@PathParam("id") String id) {
        return service.get(id)
                .onItem().transform(userOptional -> {
                    FormPage page = new FormPage();
                    page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
                    userOptional.ifPresentOrElse(
                            user -> page.addPayload(PayloadType.DOC_DATA, user),
                            () -> page.addPayload(PayloadType.DOC_DATA, "no_data")
                    );
                    return Response.ok(page).build();
                });
    }
*/
    @Route(path = "/", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            UserDTO userDTO = jsonObject.mapTo(UserDTO.class);

            service.add(userDTO).subscribe().with(
                    id -> rc.response().setStatusCode(201).end(),
                    failure -> {
                        LOGGER.error(failure.getMessage(), failure);
                        rc.response().setStatusCode(500).end(failure.getMessage());
                    }
            );
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
            UserDTO userDTO = jsonObject.mapTo(UserDTO.class);

            service.update(id, userDTO).subscribe().with(
                    updatedId -> {
                        rc.response().setStatusCode(200).end();
                    },
                    failure -> {
                        LOGGER.error(failure.getMessage(), failure);
                        rc.response().setStatusCode(500).end(failure.getMessage());
                    }
            );
        } catch (DecodeException e) {
            LOGGER.error("Error decoding request body: {}", e.getMessage());
            rc.response().setStatusCode(400).end("Invalid request body");
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) {
        String id = rc.pathParam("id");
        rc.response().setStatusCode(204).end();
    }
}
