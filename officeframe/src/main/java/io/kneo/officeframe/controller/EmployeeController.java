package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.service.EmployeeService;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

import java.util.UUID;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/employees")
public class EmployeeController extends AbstractSecuredController<Employee, EmployeeDTO> {

    @Inject
    EmployeeService service;

    public EmployeeController(UserService userService) {
        super(userService);
    }

    @Route(path = "", methods = Route.HttpMethod.GET, produces = "application/json")
    public void get(RoutingContext rc) {
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
                                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultViewActions(languageCode));
                                View<EmployeeDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return viewPage;
                            });
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );
    }

    @Route(path = "/search/:keyword", methods = Route.HttpMethod.GET, produces = "application/json")
    public void search(RoutingContext rc) {
        String keyword = rc.pathParam("keyword");
        service.search(keyword, resolveLanguage(rc))
                .onItem().transform(userList -> {
                    ViewPage viewPage = new ViewPage();
                    int pageNum = 1;
                    int pageSize = userList.size();
                    int count = userList.size();
                    View<EmployeeDTO> dtoEntries = new View<>(userList, count, pageNum, 1, pageSize);
                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                    return viewPage;
                })
                .subscribe().with(
                        page -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(page).encode()),
                        rc::fail
                );
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
    public void upsert(RoutingContext rc) throws UserNotFoundException {
        JsonObject jsonObject = rc.body().asJsonObject();
        EmployeeDTO dto = jsonObject.mapTo(EmployeeDTO.class);
        String id = rc.pathParam("id");
        service.upsert(id, dto, getUser(rc), resolveLanguage(rc))
                .subscribe().with(
                        doc -> {
                            int statusCode = (id == null || id.isEmpty()) ? 201 : 200;
                            rc.response()
                                    .setStatusCode(statusCode)
                                    .end(JsonObject.mapFrom(doc).encode());
                        },
                        rc::fail
                );
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) throws DocumentModificationAccessException, UserNotFoundException {
        String id = rc.pathParam("id");
        service.delete(id, getUser(rc))
                .subscribe().with(
                        count -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(count).encode()),
                        rc::fail
                );
    }
}