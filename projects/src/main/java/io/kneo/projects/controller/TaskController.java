package io.kneo.projects.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.dto.actions.TaskActionsFactory;
import io.kneo.projects.dto.filter.TaskFilter;
import io.kneo.projects.model.Task;
import io.kneo.projects.service.TaskService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;

import java.util.UUID;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@RouteBase(path = "/api/:org/tasks")
public final class TaskController extends AbstractSecuredController<Task, TaskDTO> {
    TaskService service;

    @Inject
    public TaskController(UserService userService, TaskService service) {
        super(userService);
        this.service = service;
    }

    @Route(path = "", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void getAll(RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "1"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));
        TaskFilter filter = rc.body().asJsonObject().mapTo(TaskFilter.class);

        try {
            IUser user = getUser(rc);

            service.getAllCount(user, filter)
                    .onItem().transformToUni(count -> {
                        int maxPage = countMaxPage(count, size);
                        int pageNum = (page == 0) ? 1 : page;
                        int offset = RuntimeUtil.calcStartEntry(pageNum, size);
                        LanguageCode languageCode = resolveLanguage(rc);
                        return service.getAll(size, offset, user, filter)
                                .onItem().transform(dtoList -> {
                                    ViewPage viewPage = new ViewPage();
                                    viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, TaskActionsFactory.getViewActions(languageCode));
                                    View<TaskDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                    return viewPage;
                                });
                    })
                    .subscribe().with(
                            viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                            rc::fail
                    );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) throws UserNotFoundException {
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

    @Route(path = "/:id?", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            TaskDTO dto = jsonObject.mapTo(TaskDTO.class);
            String id = rc.pathParam("id");
            service.upsert(UUID.fromString(id), dto, getUser(rc), resolveLanguage(rc)).subscribe().with(
                    createdTaskId -> rc.response().setStatusCode(201).end(JsonObject.mapFrom(createdTaskId).encode()),
                    rc::fail
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) throws UserNotFoundException {
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
