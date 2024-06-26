
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
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

    protected Uni<Response> getAll(IRESTService<V> service, RoutingContext rc, int page, int size) {
        Optional<IUser> userOptional = getUserId(rc);

        IUser user = userOptional.get();
        String languageHeader = rc.request().getHeader("Accept-Language");
        return service.getAllCount()
                .onItem().transformToUni(count -> {
                    int maxPage = countMaxPage(count, size);
                    int pageNum = (page == 0) ? 1 : page;
                    int offset = RuntimeUtil.calcStartEntry(pageNum, size);
                    return service.getAll(size, offset)
                            .onItem().transform(dtoList -> {
                                ViewPage viewPage = new ViewPage();
                                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultViewActions(LanguageCode.ENG));
                                View<V> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return Response.ok(viewPage).build();
                            });
                })
                .onFailure().recoverWithItem(t -> {
                    LOGGER.error("Error retrieving data: ", t);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });

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
        Uni<List<V>> unis = offsetUni.onItem().transformToUni(offset -> service.getAll(size, offset));
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

    protected void getById(IRESTService<V> service, String id, RoutingContext rc) {
        try {
            IUser user = getUser(rc);
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultFormActions(LanguageCode.ENG));
            service.getDTO(id, user, LanguageCode.ENG)
                    .subscribe().with(
                            p -> {
                                if (p == null) {
                                    throw new DocumentHasNotFoundException(id);
                                } else {
                                    page.addPayload(PayloadType.DOC_DATA, p);
                                    sendJsonResponse(rc, 200, JsonObject.mapFrom(page));
                                }
                            },
                            t -> {
                                LOGGER.error("Error retrieving DTO: ", t);
                                sendErrorResponse(rc, 500, "Internal Server Error");
                            }
                    );
        } catch (UserNotFoundException e) {
            LOGGER.warn("Authentication failed: ", e);
            sendErrorResponse(rc, 401, "Authentication failed: " + e.getMessage());
        } catch (DocumentHasNotFoundException e) {
            sendErrorResponse(rc, 404, "Not found: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error: ", e);
            sendErrorResponse(rc, 500, "An unexpected error occurred");
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

    protected Uni<Response> create(AbstractService<T, V> service, V dto, ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            return service.add(dto, user)
                    .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                    .onFailure().recoverWithItem(throwable -> {
                        LOGGER.error(throwable.getMessage());
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    });
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    protected Uni<Response> update(String id, AbstractService<T, V> service, V dto, ContainerRequestContext requestContext) throws UserNotFoundException, DocumentModificationAccessException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            return service.update(id, dto, userOptional.get())
                    .onItem().transform(count -> Response.ok(count).build());
        } else {
            return Uni.createFrom().failure(new UserNotFoundException(AnonymousUser.USER_NAME));
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

    public static class Parameters {
        @QueryParam("page")
        public int page;
    }

}
