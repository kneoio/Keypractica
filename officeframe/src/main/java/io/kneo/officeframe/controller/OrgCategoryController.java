package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.core.util.RuntimeUtil;
import io.kneo.officeframe.dto.OrgCategoryDTO;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.service.OrgCategoryService;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;
import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@ApplicationScoped
public class OrgCategoryController extends AbstractSecuredController<OrgCategory, OrgCategoryDTO> {

    private static final Logger LOGGER = Logger.getLogger(OrgCategoryController.class);

    @Inject
    OrgCategoryService service;

    public OrgCategoryController() {
        super(null);
    }

    public OrgCategoryController(UserService userService) {
        super(userService);
    }

    public void setupRoutes(Router router) {
        router.route(HttpMethod.GET, "/api/:org/orgcategories").handler(this::get);
        router.route(HttpMethod.GET, "/api/:org/orgcategories/:id").handler(this::getById);
        router.route(HttpMethod.DELETE, "/api/:org/orgcategories/:id").handler(this::delete);
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
                                View<OrgCategoryDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, size);
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
        getById(service, rc);
    }

    private void delete(RoutingContext rc)  {
        try {
            service.delete(rc.pathParam("id"), getUser(rc))
                    .subscribe().with(
                            count -> rc.response().setStatusCode(count > 0 ? 200 : 404).end(),
                            failure -> {
                                LOGGER.error("Error processing delete request: ", failure);
                                rc.fail(500, failure);
                            }
                    );
        } catch (Exception e) {
            LOGGER.error("Unexpected error: ", e);
            rc.fail(500, e);
        }
    }

    // You can uncomment and add the other methods (create, update) as needed
}