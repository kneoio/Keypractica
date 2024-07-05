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
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@RolesAllowed("**")
@RouteBase(path = "/api/:org/orgcategories")
public class OrgCategoryController extends AbstractSecuredController<OrgCategory, OrgCategoryDTO> {

    private static final Logger LOGGER = Logger.getLogger(OrgCategoryController.class);

    @Inject
    OrgCategoryService service;

    public OrgCategoryController(UserService userService) {
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

    @Route(path = "/:id", methods = Route.HttpMethod.GET, produces = "application/json")
    public void getById(RoutingContext rc) {
        try {
            getById(service, rc.pathParam("id"), rc);
        } catch (Exception e) {
            LOGGER.error("Error fetching by ID: ", e);
            rc.fail(500, e);
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.DELETE, produces = "application/json")
    public void delete(RoutingContext rc) throws UserNotFoundException {
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

    // Uncomment and modify the following methods as needed

    /*
    @Route(path = "/", methods = Route.HttpMethod.POST, consumes = "application/json", produces = "application/json")
    public void create(RoutingContext rc) {
        try {
            JsonObject jsonObject = rc.body().asJsonObject();
            OrgCategoryDTO dto = jsonObject.mapTo(OrgCategoryDTO.class);
            create(service, dto, rc).subscribe().with(
                    response -> rc.response()
                            .setStatusCode(response.getStatus())
                            .putHeader("Location", response.getHeaderString("Location"))
                            .end(),
                    failure -> {
                        LOGGER.error("Error processing request: ", failure);
                        rc.fail(500, failure);
                    }
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error: ", e);
            rc.fail(500, e);
        }
    }

    @Route(path = "/:id", methods = Route.HttpMethod.PUT, consumes = "application/json", produces = "application/json")
    public void update(RoutingContext rc) {
        try {
            String id = rc.pathParam("id");
            JsonObject jsonObject = rc.body().asJsonObject();
            OrgCategoryDTO dto = jsonObject.mapTo(OrgCategoryDTO.class);
            update(id, service, dto, rc).subscribe().with(
                    response -> rc.response()
                            .setStatusCode(response.getStatus())
                            .end(),
                    failure -> {
                        LOGGER.error("Error processing request: ", failure);
                        rc.fail(500, failure);
                    }
            );
        } catch (DocumentModificationAccessException e) {
            rc.response().setStatusCode(403).end("Access denied");
        } catch (Exception e) {
            LOGGER.error("Unexpected error: ", e);
            rc.fail(500, e);
        }
    }
    */
}
