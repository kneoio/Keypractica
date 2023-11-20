package io.kneo.core.server.security;

import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

//@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
    @Inject
    SecurityIdentity identity;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ForbiddenException exception) {
        // Set the original URL in the user's session.
        String originalUrl = uriInfo.getAbsolutePath().toString();
        identity.getAttributes().put("originalUrl", originalUrl);

        // Redirect the user to the login page.
        return Response.seeOther(UriBuilder.fromPath("/login").build()).build();
    }
}
