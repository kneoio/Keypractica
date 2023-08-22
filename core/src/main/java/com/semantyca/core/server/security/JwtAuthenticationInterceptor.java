package com.semantyca.core.server.security;

import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
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
import java.util.regex.Pattern;

@Provider
//@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationInterceptor implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger("JwtAuthenticationInterceptor");
    @Inject
    private UserRepository userRepository;

    @Context
    private JsonWebToken jwt;

    String uuidRegex = "/(projects|tasks)/[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        UriInfo uriInfo = requestContext.getUriInfo();
        String method = requestContext.getMethod();
        String path = uriInfo.getPath();
        String userName = jwt.getSubject();
        Set<String> userGroups = jwt.getGroups();
        Optional<IUser> user = userRepository.findByLogin(userName);
        if (user.isPresent() && user.get().getUserId() > 1) {
            boolean allowByGroup = switch (path) {
                case "/projects", "/tasks" -> userGroups.contains("developer");
                case "/workspace" -> true;
                default -> {
                    if (Pattern.matches(uuidRegex, path) && userGroups.contains("developer")) {
                        if (method.equals(HttpMethod.GET)) {
                            yield true;
                        } else {
                            LOGGER.error(String.format("The method \"%s\" is not allowed", method));
                            yield false;
                        }
                    } else {
                        try {
                            throw new UnknownResourceExcetion("Unknown resource: " + path);

                        } catch (UnknownResourceExcetion e) {
                            LOGGER.error(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                }
            };

            if (allowByGroup) {
                requestContext.setProperty("user", user.get());
                return;
            }
        } else {
            boolean allowAnonymously = switch (path) {
                case "/workspace", "/languages", "/employees", "/users", "/users/register", "/modules", "/roles" -> true;
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

