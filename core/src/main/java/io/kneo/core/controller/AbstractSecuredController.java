package io.kneo.core.controller;

import io.kneo.core.model.user.IUser;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.Optional;

public abstract class AbstractSecuredController<T, V> extends AbstractController<T, V> {
    protected static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final String USER_NAME_CLAIM = "preferred_username";
    protected Optional<IUser> getUserId(ContainerRequestContext requestContext) {
        DefaultJWTCallerPrincipal securityIdentity = (DefaultJWTCallerPrincipal) requestContext.getSecurityContext().getUserPrincipal();
        return userService.findByLogin(securityIdentity.getClaim(USER_NAME_CLAIM));
    }

    protected String getUserOIDCName(ContainerRequestContext requestContext) {
        DefaultJWTCallerPrincipal securityIdentity = (DefaultJWTCallerPrincipal) requestContext.getSecurityContext().getUserPrincipal();
        return securityIdentity.getClaim(USER_NAME_CLAIM);
    }

}
