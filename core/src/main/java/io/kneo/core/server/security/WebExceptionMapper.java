package io.kneo.core.server.security;

import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.util.NumberUtil;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

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
        } else {
            int errorNumber = NumberUtil.getRandomNumber(10000, 99000);
            LOGGER.error("Unexpected error occurred: {}, code: {}", exception.getMessage(), errorNumber, exception);
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
