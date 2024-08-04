package io.kneo.projects.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.dto.actions.TaskActionsFactory;
import io.kneo.projects.model.Task;
import io.kneo.projects.model.cnst.TaskStatus;
import io.kneo.projects.service.TaskService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/tasks")
public final class TaskController extends AbstractSecuredController<Task, TaskDTO> {
    TaskService service;

    @Inject
    public TaskController(UserService userService, TaskService service) {
        super(userService);
        this.service = service;
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getAll(RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "0"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));

        try {
            IUser user = getUser(rc);

            service.getAllCount(user)
                    .onItem().transformToUni(count -> {
                        int maxPage = countMaxPage(count, size);
                        int pageNum = (page == 0) ? 1 : page;
                        int offset = RuntimeUtil.calcStartEntry(pageNum, size);
                        LanguageCode languageCode = resolveLanguage(rc);
                        return service.getAll(size, offset, user)
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

    @Route(path = "/status/:status", methods = Route.HttpMethod.GET, produces = "application/json")
    @JsonView(Views.ListView.class)
    public void searchByStatus(RoutingContext rc) {
        TaskStatus status = TaskStatus.valueOf(rc.pathParam("status"));
        service.searchByStatus(status).subscribe().with(
                tasks -> {
                    ViewPage viewPage = new ViewPage();
                    viewPage.addPayload(PayloadType.VIEW_DATA, tasks);
                    rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode());
                },
                rc::fail
        );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) {
        try {
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
            service.getDTO(rc.pathParam("id"), getUser(rc), resolveLanguage(rc))
                    .onItem().transform(dto -> {
                        page.addPayload(PayloadType.DOC_DATA, dto);
                        return page;
                    })
                    .subscribe().with(
                            formPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(formPage).encode()),
                            rc::fail
                    );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            TaskDTO dto = jsonObject.mapTo(TaskDTO.class);
            service.add(dto, getUser(rc)).subscribe().with(
                    createdTaskId -> rc.response().setStatusCode(201).end(JsonObject.mapFrom(createdTaskId).encode()),
                    rc::fail
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(RoutingContext rc) {
        try {
            String id = rc.pathParam("id");
            JsonObject jsonObject = rc.body().asJsonObject();
            TaskDTO dto = jsonObject.mapTo(TaskDTO.class);
            service.update(id, dto, getUser(rc)).subscribe().with(
                    updatedTask -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(updatedTask).encode()),
                    failure -> {
                        if (failure instanceof DocumentModificationAccessException) {
                            rc.response().setStatusCode(404).end("Task not found or no modification access");
                        } else {
                            rc.fail(failure);
                        }
                    }
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) {
        try {
            service.delete(rc.pathParam("id"), getUser(rc)).subscribe().with(
                    count -> {
                        if (count > 0) {
                            rc.response().setStatusCode(204).end();
                        } else {
                            rc.fail(404);
                        }
                    },
                    rc::fail
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
