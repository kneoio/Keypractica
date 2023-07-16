package com.semantyca.core.server.security;

import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

@Provider
//@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationInterceptor implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger("JwtAuthenticationInterceptor");
    @Inject
    private UserRepository userRepository;

    @Context
    private JsonWebToken jwt;


    @Override
    public void filter(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();
        String path = uriInfo.getPath();
        String userName = jwt.getName();
        Set<String> userGroups = jwt.getGroups();
        Optional<IUser> user = userRepository.findByLogin(userName);
        if (user.isPresent() && user.get().getId() > 1) {
            boolean allowByGroup = switch (path) {
                case "/projects", "/tasks" -> userGroups.contains("developer");
                case "/workspace" -> true;
                default -> {
                    try {
                        throw new UnknownResourceExcetion("Unknown resource: " + path);
                    } catch (UnknownResourceExcetion e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            if (allowByGroup) {
                requestContext.setProperty("user", user);
                return;
            }
        } else {
            boolean allowAnonymously = switch (path) {
                case "/workspace", "/languages", "/employees", "/users", "/modules" -> true;
                default -> false;
            };
            if (allowAnonymously) {
                requestContext.setProperty("user", AnonymousUser.Build());
                return;
            } else {
                //TODO temporary allowed
                requestContext.setProperty("user", AnonymousUser.Build());
                return;
            }
        }
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Access forbidden").build());
    }
}

