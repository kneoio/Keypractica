
package io.kneo.core.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

public abstract class AbstractController<T, V> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Deprecated
    protected static final String USER_NAME_CLAIM = "preferred_username";
    protected static final String USER_NAME = "username";

    UserService userService;

    @Inject
    public AbstractController(UserService userService) {
        this.userService = userService;
    }

    protected void getAll(IRESTService<V> service, RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "0"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));
        service.getAllCount()
                .onItem().transformToUni(count -> {
                    int maxPage = countMaxPage(count, size);
                    int pageNum = (page == 0) ? 1 : page;
                    int offset = RuntimeUtil.calcStartEntry(pageNum, size);
                    LanguageCode languageCode = resolveLanguage(rc);
                    return service.getAll(size, offset, languageCode)
                            .onItem().transform(dtoList -> {
                                ViewPage viewPage = new ViewPage();
                                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultViewActions(languageCode));
                                View<V> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return viewPage;
                            });
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );

    }

    protected void getById(IRESTService<V> service, RoutingContext rc) throws UserNotFoundException {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
        service.getDTO(rc.pathParam("id"), getUser(rc), resolveLanguage(rc))
                .onItem().transform(dto -> {
                    page.addPayload(PayloadType.DOC_DATA, dto);
                    return page;
                })
                .subscribe().with(
                        formPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(formPage).encode()),
                        rc::fail
                );
    }

    protected void upsert(IRESTService<V> service, String id, RoutingContext rc) throws UserNotFoundException {
        JsonObject jsonObject = rc.body().asJsonObject();

        ObjectMapper mapper = new ObjectMapper();
        V dto = mapper.convertValue(jsonObject, new TypeReference<V>() {});

        service.upsert(id, dto, getUser(rc))
                .subscribe().with(
                        doc -> {
                            int statusCode = (id == null || id.isEmpty()) ? 201 : 200;
                            rc.response()
                                    .setStatusCode(statusCode)
                                    .end(JsonObject.mapFrom(doc).encode());
                        },
                        rc::fail
                );
    }

    @Deprecated
    protected Uni<Response> getAll(IRESTService<V> service, ContainerRequestContext requestContext, int page, int size) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);

        IUser user = userOptional.get();
        String languageHeader = requestContext.getHeaderString("Accept-Language");
        Uni<Integer> countUni = service.getAllCount();
        Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, size));
        Uni<Integer> pageNumUni = Uni.createFrom().item(page);
        Uni<Integer> offsetUni = Uni.combine().all()
                .unis(pageNumUni, Uni.createFrom().item(user.getPageSize()))
                .asTuple()
                .map(tuple -> RuntimeUtil.calcStartEntry(tuple.getItem1(), tuple.getItem2()));
        Uni<List<V>> unis = offsetUni.onItem().transformToUni(offset -> service.getAll(size, offset, LanguageCode.ENG));
        return Uni.combine().all()
                .unis(unis, offsetUni, pageNumUni, countUni, maxPageUni)
                .asTuple()
                .map(tuple -> {
                    List<V> dtoList = tuple.getItem1();
                    int offset = tuple.getItem2();
                    int pageNum = tuple.getItem3();
                    int count = tuple.getItem4();
                    int maxPage = tuple.getItem5();

                    ViewPage viewPage = new ViewPage();
                    viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultViewActions(LanguageCode.ENG));
                    if (pageNum == 0) pageNum = 1;
                    View<V> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                    return Response.ok(viewPage).build();
                });

    }

    @Deprecated
    protected Uni<Response> getById(IRESTService<V> service, String id, ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultFormActions(LanguageCode.ENG));
            return service.getDTO(id, user, LanguageCode.ENG)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.DOC_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(t -> {
                        LOGGER.error(t.getMessage(), t);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    });
        } else {
            throw new UnauthorizedException("User not authorized");
        }
    }

    private void sendJsonResponse(RoutingContext rc, int statusCode, JsonObject body) {
        rc.response()
                .setStatusCode(statusCode)
                .end(body.encode());
    }

    private void sendErrorResponse(RoutingContext rc, int statusCode, String errorMessage) {
        sendJsonResponse(rc, statusCode, new JsonObject().put("error", errorMessage));
    }

    protected Uni<Response> getDocument(AbstractService<T, V> service, String id) {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
        return service.getDTO(id, AnonymousUser.build(), LanguageCode.ENG)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.DOC_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().invoke(failure -> LOGGER.error(failure.getMessage()))
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build());
    }

    @Deprecated
    protected Optional<IUser> getUserId(ContainerRequestContext requestContext) throws UserNotFoundException {
        try {
            DefaultJWTCallerPrincipal securityIdentity = (DefaultJWTCallerPrincipal) requestContext.getSecurityContext().getUserPrincipal();
            return userService.findByLogin(securityIdentity.getClaim(USER_NAME_CLAIM));
        } catch (NullPointerException e) {
            LOGGER.warn(String.format("msg: %s ", e.getMessage()));
            throw new UserNotFoundException("User not authorized");
        } catch (Exception e) {
            LOGGER.error(String.format("msg: %s ", e.getMessage()), e);
            throw new UserNotFoundException("User not authorized");
        }
    }

    protected IUser getUser(RoutingContext rc) throws UserNotFoundException {
        try {
            User vertxUser = rc.user();
            if (vertxUser == null) {
                throw new UserNotFoundException("No user found in context");
            }

            JsonObject principal = vertxUser.principal();
            String username = principal.getString(USER_NAME);
            if (username == null) {
                throw new UserNotFoundException("Username not found in user principal");
            }

            IUser user = userService.findByLogin(username).get();
            if (user == null) {
                throw new UserNotFoundException(username);
            }

            return user;
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to get user ID: {}", e.getMessage());
            throw new UserNotFoundException("Failed to authenticate user");
        } catch (Exception e) {
            LOGGER.error("Error while getting user ID: ", e);
            throw new UserNotFoundException("An error occurred during authentication");
        }
    }

    @Deprecated
    protected Optional<IUser> getUserId(RoutingContext rc) {
        try {
            User vertxUser = rc.user();
            if (vertxUser != null) {
                JsonObject principal = vertxUser.principal();
                String username = principal.getString(USER_NAME);
                if (username != null) {
                    return userService.findByLogin(username);
                }
            }
            return Optional.empty();
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to get user ID: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Error while getting user ID: ", e);
            return Optional.empty();
        }
    }

    public Uni<Response> delete(String uuid, AbstractService<T, V> service, @Context ContainerRequestContext requestContext) throws DocumentModificationAccessException, UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.delete(uuid, userOptional.get())
                    .onItem().transform(count -> Response.ok(count).build());
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    protected Response postError(Throwable e) {
        Random rand = new Random();
        int randomNum = rand.nextInt(900000) + 100000;
        LOGGER.error(String.format("code: %s, msg: %s ", randomNum, e.getMessage()), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format("code: %s, msg: %s ", randomNum, e.getMessage())).build();
    }

    protected Response postForbidden(String userOIDCName) {
        LOGGER.warn(String.format("%s is not allowed", userOIDCName));
        return Response.status(Response.Status.FORBIDDEN)
                .entity(String.format("%s is not allowed", userOIDCName))
                .build();
    }

    protected Response postNotFoundError(Throwable e) {
        Random rand = new Random();
        int randomNum = rand.nextInt(800000) + 100000;
        LOGGER.warn(String.format("code: %s, msg: %s ", randomNum, e.getMessage()), e);
        return Response.status(Response.Status.NOT_FOUND).entity(String.format("code: %s, msg: %s ", randomNum, e.getMessage())).build();
    }

    protected static LanguageCode resolveLanguage(RoutingContext rc) {
        try {
            return LanguageCode.valueOf(rc.acceptableLanguages().getFirst().value().toUpperCase());
        } catch (Exception e) {
            return LanguageCode.ENG;
        }
    }


    public static class Parameters {
        @QueryParam("page")
        public int page;
    }

}
