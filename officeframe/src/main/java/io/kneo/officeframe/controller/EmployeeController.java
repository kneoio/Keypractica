package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.service.EmployeeService;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@ApplicationScoped
public class EmployeeController extends AbstractSecuredController<Employee, EmployeeDTO> {

    @Inject
    EmployeeService service;

    public EmployeeController() {
        super(null);
    }

    public EmployeeController(UserService userService) {
        super(userService);
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/employees").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/employees/search/:keyword").handler(this::search);
        router.route(HttpMethod.GET, "/api/:org/employees/:id").handler(this::getById);
        router.route(HttpMethod.POST, "/api/:org/employees/:id?").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/employees/:id").handler(this::delete);
    }

    private void get(RoutingContext rc) {
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

    private void search(RoutingContext rc) {
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

    private void getById(RoutingContext rc) {
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

    private void delete(RoutingContext rc)  {
        String id = rc.pathParam("id");
        service.delete(id, getUser(rc))
                .subscribe().with(
                        count -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(count).encode()),
                        rc::fail
                );
    }
}