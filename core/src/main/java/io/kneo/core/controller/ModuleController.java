package io.kneo.core.controller;

import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.ModuleService;
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
public class ModuleController extends AbstractSecuredController<Module, ModuleDTO> {

    @Inject
    ModuleService service;

    public ModuleController() {
        super(null);
    }

    public ModuleController(UserService userService, ModuleService moduleService) {
        super(userService);
        this.service = moduleService;
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/modules").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/modules/:id").handler(this::getById);
        router.route(HttpMethod.PUT, "/api/:org/modules/:id").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/modules/:id").handler(this::delete);
    }

    private void get(RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "1"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));
        LanguageCode languageCode = resolveLanguage(rc);
        IUser user;
        try {
            user = getUser(rc);
        } catch (Exception e) {
            rc.fail(401);
            return;
        }

        service.getAllCount()
                .onItem().transformToUni(count -> {
                    int maxPage = RuntimeUtil.countMaxPage(count, size);
                    int pageNum = (page == 0) ? 1 : page;
                    int offset = RuntimeUtil.calcStartEntry(pageNum, size);

                    return service.getAll(size, offset, languageCode)
                            .onItem().transform(dtoList -> {
                                ViewPage viewPage = new ViewPage();
                                View<ModuleDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, user.getPageSize());
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
        LanguageCode languageCode = resolveLanguage(rc);
        service.getDTO(UUID.fromString(id), getUser(rc), languageCode)
                .subscribe().with(
                        module -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(module).encode()),
                        rc::fail
                );
    }

    private void upsert(RoutingContext rc)  {
        String id = rc.pathParam("id");
        ModuleDTO dto = rc.body().asJsonObject().mapTo(ModuleDTO.class);
        LanguageCode languageCode = resolveLanguage(rc);

        service.upsert(id, dto, getUser(rc), languageCode)
                .subscribe().with(
                        updated -> rc.response().setStatusCode(200).end(),
                        rc::fail
                );
    }

    private void delete(RoutingContext rc) {
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