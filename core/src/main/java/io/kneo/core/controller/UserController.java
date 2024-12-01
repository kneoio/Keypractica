package io.kneo.core.controller;

import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.User;
import io.kneo.core.service.UserService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UserController extends AbstractController<User, UserDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserService service;

    public UserController() {
        super(null);
    }

    @Inject
    public UserController(UserService service) {
        super(service);
        this.service = service;
    }

    public void setupRoutes(Router router) {
        router.get("/api/:org/users").handler(this::getAll);
        router.post("/api/:org/users").handler(this::create);
        router.put("/api/:org/users/:id").handler(this::update);
        router.delete("/api/:org/users/:id").handler(this::delete);
    }

    private void getAll(RoutingContext rc) {
        Object org = rc.pathParam("org");
        ViewPage viewPage = new ViewPage();

        service.getAll()
                .subscribe().with(
                        userList -> {
                            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
                            rc.response()
                                    .setStatusCode(200)
                                    .putHeader("Content-Type", "application/json")
                                    .end(JsonObject.mapFrom(viewPage).encode());
                        },
                        failure -> {
                            LOGGER.error(failure.getMessage(), failure);
                            rc.response()
                                    .setStatusCode(500)
                                    .end(failure.getMessage());
                        }
                );
    }

    private void create(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.getBodyAsJson();
            UserDTO userDTO = jsonObject.mapTo(UserDTO.class);

            service.add(userDTO)
                    .subscribe().with(
                            id -> rc.response()
                                    .setStatusCode(201)
                                    .end(),
                            failure -> {
                                LOGGER.error(failure.getMessage(), failure);
                                rc.response()
                                        .setStatusCode(500)
                                        .end(failure.getMessage());
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Error processing request: {}", e.getMessage());
            rc.response()
                    .setStatusCode(400)
                    .end("Invalid request body");
        }
    }

    private void update(RoutingContext rc) {
        String id = rc.pathParam("id");
        try {
            JsonObject jsonObject = rc.getBodyAsJson();
            UserDTO userDTO = jsonObject.mapTo(UserDTO.class);

            service.update(id, userDTO)
                    .subscribe().with(
                            updatedId -> rc.response()
                                    .setStatusCode(200)
                                    .end(),
                            failure -> {
                                LOGGER.error(failure.getMessage(), failure);
                                rc.response()
                                        .setStatusCode(500)
                                        .end(failure.getMessage());
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Error processing request: {}", e.getMessage());
            rc.response()
                    .setStatusCode(400)
                    .end("Invalid request body");
        }
    }

    private void delete(RoutingContext rc) {
        String id = rc.pathParam("id");
        service.delete(id)
                .subscribe().with(
                        success -> rc.response()
                                .setStatusCode(204)
                                .end(),
                        failure -> {
                            LOGGER.error(failure.getMessage(), failure);
                            rc.response()
                                    .setStatusCode(500)
                                    .end(failure.getMessage());
                        }
                );
    }
}