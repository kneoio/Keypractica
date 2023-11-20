
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionBar;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.service.AbstractService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractController<T, V> {

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

    public static class Parameters {
        @QueryParam("page")
        public int page;
    }

}
