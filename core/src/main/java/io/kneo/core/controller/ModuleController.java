
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewOptionsFactory;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.service.ModuleService;
import io.kneo.core.util.RuntimeUtil;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;

import static io.kneo.core.util.RuntimeUtil.countMaxPage;

@Path("/modules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModuleController extends AbstractController<Module, ModuleDTO> {

    @Inject
    ModuleService service;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/")
    @PermitAll
    public Uni<Response> get(@BeanParam Parameters params)  {
        IUser user = new SuperUser();
        Uni<Integer> countUni = service.getAllCount();
        Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
        Uni<Integer> pageNumUni = Uni.createFrom().item(params.page);
        Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
        Uni<List<ModuleDTO>> listUni = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset));

        return Uni.combine().all().unis(listUni, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((dtoList, offset, pageNum, count, maxPage) -> {
            ViewPage viewPage = new ViewPage();
            viewPage.addPayload(PayloadType.ACTIONS, ActionsFactory.getDefault());
            viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getDefaultOptions());
            if (pageNum == 0) pageNum = 1;
            View<ModuleDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, user.getPageSize());
            viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        return getDocument(service, id);
    }

    @POST
    @Path("/")
    @RolesAllowed({"supervisor","admin"})
    public Uni<Response> create(ModuleDTO dto) {
        return service.add(dto)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @PUT
    @Path("/")
    public Response update(ModuleDTO dto) {
        return Response.ok(URI.create("/" + service.update(dto).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
