package io.kneo.core.server.security;

import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;
import java.util.NoSuchElementException;

public class GlobalErrorHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_TYPE = "application/json";

    private record ErrorResponse(int status, String message, boolean logError) {}

    private final Map<Class<? extends Throwable>, ErrorResponse> errorMappings = Map.of(
            IllegalArgumentException.class, new ErrorResponse(400, "error", false),
            DocumentHasNotFoundException.class, new ErrorResponse(404, "error", false),
            UserNotFoundException.class, new ErrorResponse(403, "error", false),
            DocumentModificationAccessException.class, new ErrorResponse(404, "error", false),
            ConnectException.class, new ErrorResponse(500, "API server error", true),
            PgException.class, new ErrorResponse(500, "API server database error", true),
            NoSuchElementException.class, new ErrorResponse(500, "Internal server error", true)
    );

    @Override
    public void handle(RoutingContext context) {
        Throwable failure = context.failure();
        Throwable rootCause = getRootCause(failure);

        ErrorResponse response = errorMappings.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(rootCause))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(new ErrorResponse(500, "Internal server error (unidentified)", true));

        if (response.logError) {
            LOGGER.error("Global error handler: ", failure);
        }

        sendErrorResponse(context, response.status, response.message);
    }

    private Throwable getRootCause(Throwable throwable) {
        return throwable.getCause() != null ? getRootCause(throwable.getCause()) : throwable;
    }

    private void sendErrorResponse(RoutingContext context, int statusCode, String message) {
        context.response()
                .setStatusCode(statusCode)
                .putHeader(CONTENT_TYPE, JSON_TYPE)
                .end(Json.encode(Map.of("error", message)));
    }
}