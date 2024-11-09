package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.officeframe.dto.TaskTypeDTO;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.service.TaskTypeService;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@ApplicationScoped
public class TaskTypeController extends AbstractSecuredController<TaskType, TaskTypeDTO> {

    @Inject
    TaskTypeService service;

    public TaskTypeController() {
        super(null);
    }

    public TaskTypeController(UserService userService, TaskTypeService service) {
        super(userService);
        this.service = service;
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/tasktypes").handler(this::getAll);
        router.route(HttpMethod.GET, "/api/:org/tasktypes/:id").handler(this::get);
        router.route(HttpMethod.POST, "/api/:org/tasktypes").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/tasktypes/:id").handler(this::delete);
    }

    private void getAll(RoutingContext rc) {
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
                                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
                                View<TaskTypeDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return viewPage;
                            });
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );
    }

    private void get(RoutingContext rc) {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());

        getContextUser(rc)
                .chain(user -> service.getDTO(UUID.fromString(rc.pathParam("id")), user, resolveLanguage(rc)))
                .onItem().transform(dto -> {
                    page.addPayload(PayloadType.DOC_DATA, dto);
                    return page;
                })
                .subscribe().with(
                        formPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(formPage).encode()),
                        error -> {
                            if (error instanceof UserNotFoundException) {
                                rc.response().setStatusCode(404).end("User not found");
                            } else {
                                rc.fail(error);
                            }
                        }
                );
    }

    private void upsert(RoutingContext rc) {
        // Implementation for upsert
    }

    private void delete(RoutingContext rc) {
        rc.response().setStatusCode(200).end();
    }
}