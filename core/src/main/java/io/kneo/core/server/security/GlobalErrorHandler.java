package io.kneo.core.server.security;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class GlobalErrorHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext context) {
        Throwable failure = context.failure();
        if (failure instanceof IllegalArgumentException) {
            context.response().setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(Map.of("error", failure.getMessage())));
        } else {
            context.next();
        }
    }
}
