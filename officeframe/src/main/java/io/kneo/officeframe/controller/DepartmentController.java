package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.officeframe.dto.DepartmentDTO;
import io.kneo.officeframe.model.Department;
import io.kneo.officeframe.service.DepartmentService;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@ApplicationScoped
public class DepartmentController extends AbstractSecuredController<Department, DepartmentDTO> {

    @Inject
    DepartmentService service;

    public DepartmentController() {
        super(null);
    }

    public DepartmentController(UserService userService, DepartmentService service) {
        super(userService);
        this.service = service;
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/departments").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/departments/only/member_of/:primary_org").handler(this::getDepartmentsOfOrg);
        router.route(HttpMethod.GET, "/api/:org/departments/:id").handler(this::getById);
        router.route(HttpMethod.POST, "/api/:org/departments").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/departments/:id").handler(this::delete);
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
                                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
                                View<DepartmentDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return viewPage;
                            });
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );
    }

    private void getDepartmentsOfOrg(RoutingContext rc) {
        LanguageCode languageCode = resolveLanguage(rc);
        service.getOfOrg(rc.pathParam("primary_org"), languageCode)
                .onItem().transform(dtoList -> {
                    ViewPage viewPage = new ViewPage();
                    int pageNum = 1;
                    int pageSize = dtoList.size();
                    int count = dtoList.size();
                    View<DepartmentDTO> dtoEntries = new View<>(dtoList, count, pageNum, 1, pageSize);
                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                    return viewPage;
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
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

    private void upsert(RoutingContext rc)  {
        upsert(service, rc.pathParam("id"), rc);
    }

    private void delete(RoutingContext rc) {
        rc.response().setStatusCode(200).end();
    }
}