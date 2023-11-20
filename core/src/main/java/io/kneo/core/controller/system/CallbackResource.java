package io.kneo.core.controller.system;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@Path("/callback")
public class CallbackResource {

    @Inject
    SecurityIdentity identity;

    @Inject
    UriInfo uriInfo;

    @GET
    public Response callback() {
        // Get the original URL from the user's session.
       // String originalUrl = (String) identity.getAttributes().get("originalUrl");

        // Redirect the user to the original URL.
        //return Response.seeOther(UriBuilder.fromUri(originalUrl).build()).build();
        return Response.seeOther(UriBuilder.fromPath("/workspace").build()).build();
    }
}