package io.kneo.core.server.security;

import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.util.NumberUtil;
import io.vertx.pgclient.PgException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;

@Provider
public class WebExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebExceptionMapper.class.getSimpleName());

    @Override
    public Response toResponse(Exception exception) {
        String json;
        Response.Status status;

        if (exception instanceof DataValidationException dve) {
            LOGGER.warn("Validation error: {}", dve.getMessage());
            json = "{\"error\":\"Validation failed: " + dve.getMessage() + "\"}";
            status = Response.Status.BAD_REQUEST;
        } else if (exception instanceof DocumentModificationAccessException dmae) {
            LOGGER.warn("Modification access error for user: {}, doc: {}", dmae.getUser(), dmae.getDocId());
            json = dmae.getMessage();
            status = Response.Status.FORBIDDEN;
        } else if (exception instanceof DocumentHasNotFoundException dhne) {
            LOGGER.warn("Document not found: {}", dhne.getMessage());
            json = "{\"error\":\"Document not found: " + dhne.getMessage() + "\"}";
            status = Response.Status.NOT_FOUND;
        } else if (exception instanceof PgException pgException) {
            int errorNumber = NumberUtil.getRandomNumber(100000, 199000);
            LOGGER.error("SQL exception: {}, code: {}", pgException.getMessage(), errorNumber, pgException);
            pgException.printStackTrace();
            json = "{\"error\":\"System exception\", \"code\": " + errorNumber + " }";
            status = Response.Status.INTERNAL_SERVER_ERROR;
        } else if (exception instanceof ConnectException connectException) {
            int errorNumber = NumberUtil.getRandomNumber(100000, 199000);
            LOGGER.error("Connect exception: {}, code: {}", connectException.getMessage(), errorNumber, connectException);
            json = "{\"error\":\"System exception\", \"code\": " + errorNumber + " }";
            status = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            int errorNumber = NumberUtil.getRandomNumber(10000, 99000);
            LOGGER.error("General exception: {}, code: {}", exception.getMessage(), errorNumber, exception);
            json = "{\"error\":\"An unexpected error occurred, please try again later.\",\"code\":" + errorNumber + "}";
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status)
                .entity(json)
                .type(MediaType.APPLICATION_JSON)
                .header("Content-Length", json.getBytes().length)
                .build();
    }
}