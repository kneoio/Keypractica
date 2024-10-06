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
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.RoleService;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.UUID;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/roles")
public class RoleController extends AbstractSecuredController<Role, RoleDTO> {

    RoleService service;

    @Inject
    public RoleController(UserService userService, RoleService roleService) {
        super(userService);
        this.service = roleService;
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) throws UserNotFoundException {
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

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) {
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

    @Route(path = "/", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        RoleDTO dto = rc.body().asJsonObject().mapTo(RoleDTO.class);

        service.add(dto)
                .subscribe().with(
                        id -> rc.response().setStatusCode(201).end(),
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(RoutingContext rc) {
        String id = rc.pathParam("id");
        RoleDTO dto = rc.body().asJsonObject().mapTo(RoleDTO.class);

        service.update(id, dto)
                .subscribe().with(
                        res -> rc.response().setStatusCode(200).end(),
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) {
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
