package io.kneo.core.controller;

import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.ModuleService;
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
@RouteBase(path = "/api/:org/modules")
public class ModuleController extends AbstractSecuredController<Module, ModuleDTO> {

    ModuleService service;

    @Inject
    public ModuleController(UserService userService, ModuleService moduleService) {
        super(userService);
        this.service = moduleService;
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) {
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

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) throws UserNotFoundException {
        String id = rc.pathParam("id");
        LanguageCode languageCode = resolveLanguage(rc);
        service.getDTO(UUID.fromString(id), getUser(rc), languageCode)
                .subscribe().with(
                        module -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(module).encode()),
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.PUT, consumes = "application/json", produces = "application/json")
    public void upsert(RoutingContext rc) throws UserNotFoundException {
        String id = rc.pathParam("id");
        ModuleDTO dto = rc.body().asJsonObject().mapTo(ModuleDTO.class);
        LanguageCode languageCode = resolveLanguage(rc);

        service.upsert(id, dto, getUser(rc), languageCode)
                .subscribe().with(
                        updated -> rc.response().setStatusCode(200).end(),
                        rc::fail
                );
    }
    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) throws UserNotFoundException, DocumentModificationAccessException {
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
