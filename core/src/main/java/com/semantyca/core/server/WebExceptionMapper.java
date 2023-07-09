package com.semantyca.core.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class WebExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebExceptionMapper.class.getSimpleName());
    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error(exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorDetails("An unexpected error occurred, please try again later."))
                .build();
    }

    public static class ErrorDetails {
        private String message;

        public ErrorDetails(String message) {
            this.message = message;
        }

    }
}
