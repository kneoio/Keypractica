package io.kneo.core.server.security;

import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.NoSuchElementException;

public class GlobalErrorHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebExceptionMapper.class.getSimpleName());

    @Override
    public void handle(RoutingContext context) {
        Throwable failure = context.failure();
        if (failure instanceof IllegalArgumentException) {
            context.response().setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(Map.of("error", failure.getMessage())));
        } else if (failure instanceof DocumentHasNotFoundException) {
            context.response().setStatusCode(404)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(Map.of("error", failure.getMessage())));
        } else if (failure instanceof UserNotFoundException) {
            context.response().setStatusCode(403)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(Map.of("error", failure.getMessage())));
        } else if (failure instanceof NoSuchElementException) {
            LOGGER.error("Global error handler: " + failure);
            failure.printStackTrace();
            context.response().setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(Json.encode(Map.of("error", "Internal server error")));
        } else {
            context.next();
        }
    }
}
