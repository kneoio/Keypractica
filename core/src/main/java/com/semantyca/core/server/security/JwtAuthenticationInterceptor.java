package com.semantyca.core.server.security;

import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.User;
import com.semantyca.core.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Provider
//@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationInterceptor implements ContainerRequestFilter {
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
        Optional<User> user =  userRepository.findByLogin(userName)
                .await().atMost(Duration.ofSeconds(30));

        if (user.isPresent()) {
            boolean allowByGroup = switch (path) {
                case "/projects" -> userGroups.contains("developer");
                case "/tasks" -> userGroups.contains("developer");
                case "/workspace" -> true;
                default -> {
                    try {
                        throw new UnknownResourceExcetion("Unknown resource: " + path);
                    } catch (UnknownResourceExcetion e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            if (allowByGroup){
                requestContext.setProperty("user", user.get());
                return;
            }
        } else {
            boolean allowAnonymously = switch (path) {
                case "/workspace" -> true;
                default -> false;
            };
            if (allowAnonymously) {
                requestContext.setProperty("user", AnonymousUser.Build());
                return;
            }
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Access forbidden").build());
        }

    }
}
