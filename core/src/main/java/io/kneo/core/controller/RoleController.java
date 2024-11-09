package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.RoleDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.Role;
import io.kneo.core.service.RoleService;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class RoleController extends AbstractSecuredController<Role, RoleDTO> {

    @Inject
    RoleService service;

    public RoleController() {
        super(null);
    }

    public RoleController(UserService userService, RoleService roleService) {
        super(userService);
        this.service = roleService;
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/roles").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/roles/:id").handler(this::getById);
        router.route(HttpMethod.POST, "/api/:org/roles").handler(this::create);
        router.route(HttpMethod.PUT, "/api/:org/roles/:id").handler(this::update);
        router.route(HttpMethod.DELETE, "/api/:org/roles/:id").handler(this::delete);
    }

    private void get(RoutingContext rc)  {
        int page = Integer.parseInt(rc.request().getParam("page", "1"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));
        LanguageCode languageCode = resolveLanguage(rc);
        IUser user = getUser(rc);

        service.getAllCount()
                .onItem().transformToUni(count -> {
                    int maxPage = RuntimeUtil.countMaxPage(count, size);
                    int pageNum = (page == 0) ? 1 : page;
                    int offset = RuntimeUtil.calcStartEntry(pageNum, size);

                    return service.getAll(size, offset)
                            .onItem().transform(dtoList -> {
                                ViewPage viewPage = new ViewPage();
                                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, languageCode);
                                View<RoleDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, user.getPageSize());
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return viewPage;
                            });
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );
    }

    private void getById(RoutingContext rc) {
        String id = rc.pathParam("id");
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());

        service.getDTO(UUID.fromString(id), AnonymousUser.build(), LanguageCode.ENG)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.DOC_DATA, p);
                    return page;
                })
                .subscribe().with(
                        formPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(formPage).encode()),
                        rc::fail
                );
    }

    private void create(RoutingContext rc) {
        RoleDTO dto = rc.body().asJsonObject().mapTo(RoleDTO.class);

        service.add(dto)
                .subscribe().with(
                        id -> rc.response().setStatusCode(201).end(),
                        rc::fail
                );
    }

    private void update(RoutingContext rc) {
        String id = rc.pathParam("id");
        RoleDTO dto = rc.body().asJsonObject().mapTo(RoleDTO.class);

        service.update(id, dto)
                .subscribe().with(
                        res -> rc.response().setStatusCode(200).end(),
                        rc::fail
                );
    }

    private void delete(RoutingContext rc) {
        String id = rc.pathParam("id");

        service.delete(id)
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