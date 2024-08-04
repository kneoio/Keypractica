package io.kneo.projects.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.projects.dto.ai.PromptDTO;
import io.kneo.projects.service.AiService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/ai")
public class AiController extends AbstractSecuredController<Object, PromptDTO> {

    @Inject
    AiService service;

    public AiController(UserService userService) {
        super(userService);
    }

    @Route(path = "/chat", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void chat(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            PromptDTO promptDTO = jsonObject.mapTo(PromptDTO.class);

            service.chat(promptDTO, getUser(rc))
                    .subscribe().with(
                            response -> rc.response().setStatusCode(200).end(response),
                            rc::fail
                    );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
