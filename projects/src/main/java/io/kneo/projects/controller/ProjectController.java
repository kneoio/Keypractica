package io.kneo.projects.controller;

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
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.actions.ProjectActionsFactory;
import io.kneo.projects.model.Project;
import io.kneo.projects.service.ProjectService;
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
@RouteBase(path = "/api/:org/projects")
public class ProjectController extends AbstractSecuredController<Project, ProjectDTO> {

    @Inject
    ProjectService service;

    public ProjectController(UserService userService) {
        super(userService);
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) {
        int page = Integer.parseInt(rc.request().getParam("page", "1"));
        int size = Integer.parseInt(rc.request().getParam("size", "10"));

        try {
            IUser user = getUser(rc);

            Uni.combine().all().unis(
                    service.getAllCount(user),
                    service.getAll(size, (page - 1) * size, user)
            ).asTuple().subscribe().with(
                    tuple -> {
                        int count = tuple.getItem1();
                        List<ProjectDTO> projects = tuple.getItem2();

                        int maxPage = RuntimeUtil.countMaxPage(count, size);

                        ViewPage viewPage = new ViewPage();
                        View<ProjectDTO> dtoEntries = new View<>(projects, count, page, maxPage, size);
                        viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);

                        ActionBox actions = ProjectActionsFactory.getViewActions(user.getActivatedRoles());
                        viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, actions);

                        rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode());
                    },
                    failure -> {
                        LOGGER.error("Error processing request: ", failure);
                        rc.response().setStatusCode(500).end("Internal Server Error");
                    }
            );
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Route(path = "/search/:keyword", methods = Route.HttpMethod.GET, produces = "application/json")
    public void search(RoutingContext rc) {
        String keyword = rc.pathParam("keyword");
        service.search(keyword).subscribe().with(
                projects -> {
                    ViewPage viewPage = new ViewPage();
                    viewPage.addPayload(PayloadType.VIEW_DATA, projects);
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
                    project -> {
                        FormPage page = new FormPage();
                        page.addPayload(PayloadType.DOC_DATA, project);
                        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
                        rc.response().setStatusCode(200).end(JsonObject.mapFrom(page).encode());
                    },
                    failure -> {
                        if (failure instanceof DocumentHasNotFoundException) {
                            rc.response().setStatusCode(404).end("Project not found");
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

    @Route(path = "/:id", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void upsert(RoutingContext rc) {
        try {
            String id = rc.pathParam("id");
            JsonObject jsonObject = rc.body().asJsonObject();
            ProjectDTO dto = jsonObject.mapTo(ProjectDTO.class);
            service.upsert(UUID.fromString(id), dto, getUser(rc), resolveLanguage(rc))
                    .subscribe().with(
                            createdProjectId -> rc.response().setStatusCode(200).end(createdProjectId.toString()),
                            failure -> {
                                if (failure instanceof RuntimeException) {
                                    throw (RuntimeException) failure;
                                } else {
                                    throw new RuntimeException(failure);
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
