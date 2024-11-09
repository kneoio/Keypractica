package io.kneo.qtracker.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.qtracker.dto.ConsumingDTO;
import io.kneo.qtracker.dto.actions.ConsumingActionsFactory;
import io.kneo.qtracker.model.Consuming;
import io.kneo.qtracker.service.ConsumingService;
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
public class ConsumingController extends AbstractSecuredController<Consuming, ConsumingDTO> {

    @Inject
    ConsumingService service;

    public ConsumingController() {
        super(null);
    }

    public ConsumingController(UserService userService) {
        super(userService);
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/consumings").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/consumings/:messengerType/:userName").handler(this::getMine);
        router.route(HttpMethod.GET, "/api/:org/consumings/:id").handler(this::getById);
        router.route(HttpMethod.POST, "/api/:org/consumings/:id?").handler(this::upsert);
        router.route(HttpMethod.POST, "/api/:org/consumings/add/:id?").handler(this::insertAndCalc);
        router.route(HttpMethod.DELETE, "/api/:org/consumings/:id").handler(this::delete);
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
                    List<ConsumingDTO> consumings = tuple.getItem2();

                    int maxPage = RuntimeUtil.countMaxPage(count, size);

                    ViewPage viewPage = new ViewPage();
                    View<ConsumingDTO> dtoEntries = new View<>(consumings, count, page, maxPage, size);
                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);

                    ActionBox actions = ConsumingActionsFactory.getViewActions(user.getActivatedRoles());
                    viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, actions);

                    rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode());
                },
                rc::fail
        );
    }

    private void getMine(RoutingContext rc) {
        IUser user = getUser(rc);
        String userName = rc.pathParam("userName");
        service.getAllMine(userName, user)
                .subscribe().with(
                        consumings -> {
                            int count = consumings.size();
                            ViewPage viewPage = new ViewPage();
                            View<ConsumingDTO> dtoEntries = new View<>(consumings, count, 1, 1, count);
                            viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                            rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode());
                        },
                        rc::fail
                );
    }

    private void getById(RoutingContext rc)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
        service.getDTO(UUID.fromString(rc.pathParam("id")), getUser(rc), resolveLanguage(rc))
                .onItem().transform(dto -> {
                    page.addPayload(PayloadType.DOC_DATA, dto);
                    return page;
                })
                .subscribe().with(
                        formPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(formPage).encode()),
                        rc::fail
                );
    }

    private void upsert(RoutingContext rc) {
        String id = rc.pathParam("id");
        IUser user = getUser(rc);

        JsonObject jsonObject = rc.body().asJsonObject();
        ConsumingDTO dto = jsonObject.mapTo(ConsumingDTO.class);
        service.upsert(id, dto, user, resolveLanguage(rc))
                .subscribe().with(
                        doc -> {
                            int statusCode = id == null ? 201 : 200;
                            rc.response().setStatusCode(statusCode).end(JsonObject.mapFrom(doc).encode());
                        },
                        rc::fail
                );
    }

    private void insertAndCalc(RoutingContext rc)  {
        String id = rc.pathParam("id");
        IUser user = getUser(rc);

        JsonObject jsonObject = rc.body().asJsonObject();
        ConsumingDTO dto = jsonObject.mapTo(ConsumingDTO.class);
        service.insertAndProcess(id, dto, user, resolveLanguage(rc))
                .subscribe().with(
                        doc -> {
                            int statusCode = id == null ? 201 : 200;
                            rc.response().setStatusCode(statusCode).end(JsonObject.mapFrom(doc).encode());
                        },
                        rc::fail
                );
    }

    private void delete(RoutingContext rc)  {
        service.delete(rc.pathParam("id"), getUser(rc))
                .subscribe().with(
                        count -> {
                            if (count > 0) {
                                rc.response().setStatusCode(200).end();
                            } else {
                                rc.fail(404);
                            }
                        },
                        rc::fail
                );
    }
}