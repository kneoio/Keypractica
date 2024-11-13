package io.kneo.core.server.security;

import io.kneo.core.util.NumberUtil;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WebExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        String json;
        Response.Status status;

        int errorNumber = NumberUtil.getRandomNumber(10000, 99000);
        LOGGER.error("Core exception: {}, code: {}", exception.getMessage(), errorNumber, exception);
        json = "{\"error\": \"Unexpected error\", \"message\": \"Core error, please try again later.\", \"code\": " + errorNumber + "}";
        status = Response.Status.INTERNAL_SERVER_ERROR;


        return Response.status(status)
                .entity(json)
                .type(MediaType.APPLICATION_JSON)
                .header("Content-Length", json.getBytes().length)
                .build();
    }
}
