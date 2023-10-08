package com.semantyca.core.controller;

import com.semantyca.core.dto.RoleDTO;
import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.actions.ActionsFactory;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.form.FormPage;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.model.user.Role;
import com.semantyca.core.model.user.SuperUser;
import com.semantyca.core.service.RoleService;
import com.semantyca.core.util.RuntimeUtil;
import io.smallrye.mutiny.Uni;
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

import java.util.List;

import static com.semantyca.core.util.RuntimeUtil.countMaxPage;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleController extends AbstractController<Role, RoleDTO> {
    @Inject
    RoleService service;

    @GET
    @Path("/")
    public Uni<Response> get(@BeanParam Parameters params)  {
        IUser user = new SuperUser();
        Uni<Integer> countUni = service.getAllCount();
        Uni<Integer> maxPageUni = countUni.onItem().transform(c -> countMaxPage(c, user.getPageSize()));
        Uni<Integer> pageNumUni = Uni.createFrom().item(params.page);
        Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
        Uni<List<RoleDTO>> listUni = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset));
        return Uni.combine().all().unis(listUni, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((dtoList, offset, pageNum, count, maxPage) -> {
            ViewPage viewPage = new ViewPage();
            viewPage.addPayload(PayloadType.ACTIONS, ActionsFactory.getDefault());
            viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getDefaultOptions());
            if (pageNum == 0) pageNum = 1;
            View<RoleDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, user.getPageSize());
            viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        return service.get(id)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @POST
    @Path("/")
    public Uni<Response> create(RoleDTO dto) {
        return service.add(dto)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(RoleDTO dto, @PathParam("id") String id) {
        return service.update(id, dto)
                .onItem().transform(res -> Response.status(Response.Status.OK).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@PathParam("id") String id) {
        return service.delete(id)
                .onItem().transformToUni(result -> Uni.createFrom().voidItem())
                .onItem().transform(ignore -> Response.status(Response.Status.OK).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

}
