
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

public abstract class AbstractController<T, V> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    protected static final String USER_NAME_CLAIM = "preferred_username";
    @Inject
    UserService userService;

    protected Uni<Response> getAll(IRESTService<V> service, ContainerRequestContext requestContext, int page) {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            Uni<Integer> countUni = service.getAllCount();
            Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
            Uni<Integer> pageNumUni = Uni.createFrom().item(page);
            Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
            Uni<List<V>> unis = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset));
            return Uni.combine().all().unis(unis, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((dtoList, offset, pageNum, count, maxPage) -> {
                ViewPage viewPage = new ViewPage();
                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefault());
                if (pageNum == 0) pageNum = 1;
                View<V> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, user.getPageSize());
                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                return Response.ok(viewPage).build();
            });
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }

    }
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

    protected Optional<IUser> getUserId(ContainerRequestContext requestContext) {
        try {
            DefaultJWTCallerPrincipal securityIdentity = (DefaultJWTCallerPrincipal) requestContext.getSecurityContext().getUserPrincipal();
            return userService.findByLogin(securityIdentity.getClaim(USER_NAME_CLAIM));
        } catch (NullPointerException e) {
            LOGGER.warn(String.format("msg: %s ", e.getMessage()));
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error(String.format("msg: %s ", e.getMessage()), e);
            return Optional.empty();
        }
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
