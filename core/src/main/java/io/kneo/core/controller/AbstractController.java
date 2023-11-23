
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionBar;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.model.cnst.RoleType;
import io.kneo.core.service.AbstractService;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractController<T, V> {
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    protected Uni<Response> getDocument(AbstractService<T, V> service, String id) {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        return service.get(id)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().invoke(failure -> LOGGER.error(failure.getMessage()))
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build());
    }

    protected boolean isSupervisor(OidcJwtCallerPrincipal securityIdentity) {
        if (securityIdentity != null) {
            JsonObject roles = securityIdentity.getClaim(REALM_ACCESS_CLAIM);
            JsonValue jsonValue = roles.get("roles");
            return jsonValue.asJsonArray().stream()
                    .map(Object::toString)
                    .anyMatch(s -> s.equals("\"" + RoleType.SUPERVISOR.getName() + "\""));
        }
        return false;
    }

    protected String getUserName(OidcJwtCallerPrincipal securityIdentity) {
        return securityIdentity.getClaim("preferred_username");
    }

    public static class Parameters {
        @QueryParam("page")
        public int page;
    }

}
