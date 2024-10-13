package io.kneo.qtracker.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.qtracker.dto.OwnerDTO;
import io.kneo.qtracker.dto.actions.OwnerActionsFactory;
import io.kneo.qtracker.model.Owner;
import io.kneo.qtracker.service.OwnerService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/owners")
public class OwnerController extends AbstractSecuredController<Owner, OwnerDTO> {

    @Inject
    OwnerService service;

    public OwnerController(UserService userService) {
        super(userService);
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) throws UserNotFoundException {
        int page = Integer.parseInt(rc.request().getParam("page", "1"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));
        IUser user = getUser(rc);

        Uni.combine().all().unis(
                service.getAllCount(user),
                service.getAll(size, (page - 1) * size, user)
        ).asTuple().subscribe().with(
                tuple -> {
                    int count = tuple.getItem1();
                    List<OwnerDTO> owners = tuple.getItem2();

                    int maxPage = RuntimeUtil.countMaxPage(count, size);

                    ViewPage viewPage = new ViewPage();
                    View<OwnerDTO> dtoEntries = new View<>(owners, count, page, maxPage, size);
                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);

                    ActionBox actions = OwnerActionsFactory.getViewActions(user.getActivatedRoles());
                    viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, actions);

                    rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode());
                },
                failure -> {
                    LOGGER.error("Error processing request: ", failure);
                    rc.response().setStatusCode(500).end("Internal Server Error");
                }
        );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) {
        try {
            String id = rc.pathParam("id");
            LanguageCode languageCode = LanguageCode.valueOf(rc.request().getParam("lang", LanguageCode.ENG.name()));

            service.getDTO(UUID.fromString(id), getUser(rc), languageCode).subscribe().with(
                    owner -> {
                        FormPage page = new FormPage();
                        page.addPayload(PayloadType.DOC_DATA, owner);
                        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
                        rc.response().setStatusCode(200).end(JsonObject.mapFrom(page).encode());
                    },
                    failure -> {
                        if (failure instanceof DocumentHasNotFoundException) {
                            rc.response().setStatusCode(404).end("Owner not found");
                        } else {
                            LOGGER.error("Error processing request: ", failure);
                            rc.response().setStatusCode(500).end("Internal Server Error");
                        }
                    }
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "/:id?", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void upsert(RoutingContext rc) throws UserNotFoundException {
        String id = rc.pathParam("id");
        JsonObject jsonObject = rc.body().asJsonObject();
        OwnerDTO dto = jsonObject.mapTo(OwnerDTO.class);
        service.upsert(id, dto, getUser(rc), resolveLanguage(rc))
                .subscribe().with(
                        createdOwnerId -> rc.response().setStatusCode(200).end(createdOwnerId.toString()),
                        failure -> {
                            if (failure instanceof RuntimeException) {
                                throw (RuntimeException) failure;
                            } else {
                                throw new RuntimeException(failure);
                            }
                        }
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) {
        try {
            String id = rc.pathParam("id");
            service.delete(id, getUser(rc)).subscribe().with(
                    count -> rc.response().setStatusCode(count > 0 ? 204 : 404).end(),
                    failure -> {
                        LOGGER.error(failure.getMessage(), failure);
                        rc.response().setStatusCode(500).end("Internal Server Error");
                    }
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
