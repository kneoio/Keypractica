
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public abstract class AbstractController<T, V> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Inject
    UserService userService;
    protected Uni<Response> getDocument(AbstractService<T, V> service, String id) {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
        return service.get(id)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().invoke(failure -> LOGGER.error(failure.getMessage()))
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build());
    }

    protected Response postError(Throwable e) {
        Random rand = new Random();
        int randomNum = rand.nextInt(900000) + 100000;
        LOGGER.error(String.format("code: %s, msg: %s ", randomNum, e.getMessage()), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format("code: %s, msg: %s ", randomNum, e.getMessage())).build();
    }

    public static class Parameters {
        @QueryParam("page")
        public int page;
    }

}
