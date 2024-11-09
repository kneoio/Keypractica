package io.kneo.qtracker.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.qtracker.dto.OwnerDTO;
import io.kneo.qtracker.dto.actions.OwnerActionsFactory;
import io.kneo.qtracker.model.Owner;
import io.kneo.qtracker.service.OwnerService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OwnerController extends AbstractSecuredController<Owner, OwnerDTO> {

    @Inject
    OwnerService service;

    public OwnerController() {
        super(null);
    }

    public OwnerController(UserService userService) {
        super(userService);
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/owners").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/owners/telegram/:telegram_id").handler(this::getByTelegramId);
        router.route(HttpMethod.GET, "/api/:org/owners/:id").handler(this::getById);
        router.route(HttpMethod.POST, "/api/:org/owners/:messengerType/:id?").handler(this::upsertMessengerUser);
        router.route(HttpMethod.DELETE, "/api/:org/owners/:id").handler(this::delete);
    }

    private void get(RoutingContext rc) {
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
                rc::fail
        );
    }

    private void getByTelegramId(RoutingContext rc) {
        String id = rc.pathParam("telegram_id");
        LanguageCode languageCode = LanguageCode.valueOf(rc.request().getParam("lang", LanguageCode.ENG.name()));

        service.getDTOByTelegramId(id, getUser(rc), languageCode).subscribe().with(
                owner -> {
                    FormPage page = new FormPage();
                    page.addPayload(PayloadType.DOC_DATA, owner);
                    rc.response().setStatusCode(200).end(JsonObject.mapFrom(page).encode());
                },
                rc::fail
        );
    }

    private void getById(RoutingContext rc) {
        String id = rc.pathParam("id");
        LanguageCode languageCode = LanguageCode.valueOf(rc.request().getParam("lang", LanguageCode.ENG.name()));

        service.getDTO(UUID.fromString(id), getUser(rc), languageCode).subscribe().with(
                owner -> {
                    FormPage page = new FormPage();
                    page.addPayload(PayloadType.DOC_DATA, owner);
                    page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
                    rc.response().setStatusCode(200).end(JsonObject.mapFrom(page).encode());
                },
                rc::fail
        );
    }

    private void upsertMessengerUser(RoutingContext rc) {
        String id = rc.pathParam("id");
        String messengerType = rc.pathParam("messengerType");
        JsonObject jsonObject = rc.body().asJsonObject();
        OwnerDTO dto = jsonObject.mapTo(OwnerDTO.class);
        service.upsert(id, dto, getUser(rc), LanguageCode.ENG)
                .subscribe().with(
                        doc -> {
                            int statusCode = id == null ? 201 : 200;
                            rc.response().setStatusCode(statusCode).end(JsonObject.mapFrom(doc).encode());
                        },
                        rc::fail
                );
    }

    private void delete(RoutingContext rc)  {
        String id = rc.pathParam("id");
        service.delete(id, getUser(rc)).subscribe().with(
                count -> rc.response().setStatusCode(count > 0 ? 204 : 404).end(),
                rc::fail
        );
    }
}