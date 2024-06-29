package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.RoleDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.Role;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.RoleService;
import io.kneo.core.util.RuntimeUtil;
import io.smallrye.mutiny.Uni;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class RoleController extends AbstractSecuredController<Role, RoleDTO> {
    @Inject
    RoleService service;

    @GET
    @Path("/")
    public Uni<Response> get(@BeanParam Parameters params, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            Uni<Integer> countUni = service.getAllCount();
            Uni<Integer> maxPageUni = countUni.onItem().transform(c -> RuntimeUtil.countMaxPage(c, user.getPageSize()));
            Uni<Integer> pageNumUni = Uni.createFrom().item(params.page);
            Uni<Integer> offsetUni = Uni.combine().all().unis(pageNumUni, Uni.createFrom().item(user.getPageSize())).combinedWith(RuntimeUtil::calcStartEntry);
            Uni<List<RoleDTO>> listUni = offsetUni.onItem().transformToUni(offset -> service.getAll(user.getPageSize(), offset));
            return Uni.combine().all().unis(listUni, offsetUni, pageNumUni, countUni, maxPageUni).combinedWith((dtoList, offset, pageNum, count, maxPage) -> {
                ViewPage viewPage = new ViewPage();
                viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, ActionsFactory.getDefaultViewActions(LanguageCode.ENG));
                if (pageNum == 0) pageNum = 1;
                View<RoleDTO> dtoEntries = new View<>(dtoList, count, pageNum, maxPage, user.getPageSize());
                viewPage.addPayload(PayloadType.VIEW_DATA, dtoEntries);
                return Response.ok(viewPage).build();
            });
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
        return service.getDTO(id, AnonymousUser.build(), LanguageCode.ENG)
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.DOC_DATA, p);
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
