import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.util.NumberUtil;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.UnauthorizedException;
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

        switch (exception) {
            case DataValidationException dve -> {
                LOGGER.warn("Validation error: {}", dve.getMessage());
                json = "{\"error\": \"Validation failed\", \"message\": \"" + dve.getMessage() + "\"}";
                return Response.status(422)
                        .entity(json)
                        .type(MediaType.APPLICATION_JSON)
                        .header("Content-Length", json.getBytes().length)
                        .build();
            }
            case DocumentModificationAccessException dmae -> {
                LOGGER.warn("Modification access error for user: {}, doc: {}", dmae.getUser(), dmae.getDocId());
                json = "{\"error\": \"Access denied\", \"message\": \"" + dmae.getMessage() + "\"}";
                status = Response.Status.FORBIDDEN;
            }
            case DocumentHasNotFoundException dhne -> {
                LOGGER.warn("Document not found: {}", dhne.getMessage());
                json = "{\"error\": \"Document not found\", \"message\": \"" + dhne.getMessage() + "\"}";
                status = Response.Status.NOT_FOUND;
            }
            case PgException pgException -> {
                int errorNumber = NumberUtil.getRandomNumber(100000, 199000);
                LOGGER.error("SQL exception: {}, code: {}", pgException.getMessage(), errorNumber, pgException);
                json = "{\"error\": \"System exception\", \"message\": \"Database error occurred.\", \"code\": " + errorNumber + "}";
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            case ConnectException connectException -> {
                int errorNumber = NumberUtil.getRandomNumber(100000, 199000);
                LOGGER.error("Connect exception: {}, code: {}", connectException.getMessage(), errorNumber, connectException);
                json = "{\"error\": \"System exception\", \"message\": \"Connection error occurred.\", \"code\": " + errorNumber + "}";
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            case UnauthorizedException ue -> {
                LOGGER.warn("Unauthorized access attempt: {}", ue.getMessage());
                json = "{\"error\": \"Unauthorized\", \"message\": \"You must be logged in to access this resource.\"}";
                status = Response.Status.UNAUTHORIZED;
            }
            case ForbiddenException fe -> {
                LOGGER.warn("Forbidden access attempt: {}", fe.getMessage());
                json = "{\"error\": \"Forbidden\", \"message\": \"You do not have the required permissions to access this resource.\"}";
                status = Response.Status.FORBIDDEN;
            }
            case null, default -> {
                int errorNumber = NumberUtil.getRandomNumber(10000, 99000);
                LOGGER.error("General exception: {}, code: {}", exception.getMessage(), errorNumber, exception);
                json = "{\"error\": \"Unexpected error\", \"message\": \"An unexpected error occurred, please try again later.\", \"code\": " + errorNumber + "}";
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
        }

        return Response.status(status)
                .entity(json)
                .type(MediaType.APPLICATION_JSON)
                .header("Content-Length", json.getBytes().length)
                .build();
    }
}
