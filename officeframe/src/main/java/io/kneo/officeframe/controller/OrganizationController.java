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
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.service.OrganizationService;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@ApplicationScoped
public class OrganizationController extends AbstractSecuredController<Organization, OrganizationDTO> {

    @Inject
    OrganizationService service;

    public OrganizationController() {
        super(null);
    }

    public OrganizationController(UserService userService, OrganizationService service) {
        super(userService);
        this.service = service;
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/orgs").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/orgs/only/primary").handler(this::getPrimary);
        router.route(HttpMethod.GET, "/api/:org/orgs/:id").handler(this::getById);
        router.route(HttpMethod.POST, "/api/:org/orgs/:id?").handler(this::upsert);
        router.route(HttpMethod.DELETE, "/api/:org/orgs/:id").handler(this::delete);
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
                                View<OrganizationDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
                                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                                return viewPage;
                            });
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );
    }

    private void getPrimary(RoutingContext rc) {
        LanguageCode languageCode = resolveLanguage(rc);

        service.getPrimary(languageCode)
                .onItem().transform(dtoList -> {
                    ViewPage viewPage = new ViewPage();
                    viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultViewActions(languageCode));
                    int pageNum = 1;
                    int pageSize = dtoList.size();
                    int count = dtoList.size();
                    View<OrganizationDTO> dtoEntries = new View<>(dtoList, count, pageNum, 1, pageSize);
                    viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                    return viewPage;
                })
                .subscribe().with(
                        viewPage -> rc.response().setStatusCode(200).end(JsonObject.mapFrom(viewPage).encode()),
                        rc::fail
                );
    }

    private void getById(RoutingContext rc)  {
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
        JsonObject jsonObject = rc.body().asJsonObject();
        OrganizationDTO dto = jsonObject.mapTo(OrganizationDTO.class);
        String id = rc.pathParam("id");
        service.upsert(id, dto, getUser(rc), LanguageCode.ENG)
                .subscribe().with(
                        organization -> {
                            int statusCode = id.isEmpty() ? 201 : 200;
                            rc.response().setStatusCode(statusCode).end(JsonObject.mapFrom(organization).encode());
                        },
                        rc::fail
                );
    }

    private void delete(RoutingContext rc)  {
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