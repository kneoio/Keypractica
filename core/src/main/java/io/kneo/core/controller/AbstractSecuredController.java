package io.kneo.core.controller;

import io.kneo.core.service.UserService;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.ws.rs.container.ContainerRequestContext;

public abstract class AbstractSecuredController<T, V> extends AbstractController<T, V> {
    protected static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    public AbstractSecuredController(UserService userService) {
        super(userService);
    }

    @Deprecated
    protected String getUserOIDCName(ContainerRequestContext requestContext) {
        DefaultJWTCallerPrincipal securityIdentity = (DefaultJWTCallerPrincipal) requestContext.getSecurityContext().getUserPrincipal();
        return securityIdentity.getClaim(USER_NAME_CLAIM);
    }

}
