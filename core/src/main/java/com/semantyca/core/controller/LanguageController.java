
package com.semantyca.core.controller;

import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.form.FormPage;
import com.semantyca.core.dto.view.View;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.service.LanguageService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
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

@Path("/languages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LanguageController extends AbstractController {

    @Inject
    LanguageService service;

    @GET
    @Path("/")
    @PermitAll
    public Response get()  {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.ACTIONS, new ActionBar());
        viewPage.addPayload(PayloadType.VIEW_OPTIONS, ViewOptionsFactory.getProjectOptions());
        View<LanguageDTO> view = new View<>(service.getAll(100, 0).await().indefinitely());
        viewPage.addPayload(PayloadType.VIEW_DATA, view);
        return Response.ok(viewPage).build();
    }

    @GET
    @Path("/code/{code}")
    public Response getByCode(@PathParam("code") String code)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        page.addPayload(PayloadType.FORM_DATA, service.findByCode(code.toUpperCase()).await().indefinitely());
        return Response.ok(page).build();
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
                .onFailure().invoke(failure -> LOGGER.error(failure.getMessage()))
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build());
    }

    @POST
    public Uni<Response> create(LanguageDTO dto) {
        return service.add(dto)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(LanguageDTO dto, String id) {
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

    @DELETE
    @Path("/code/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> deleteByCode(@PathParam("code") String code) {
        return service.deleteByCode(code)
                .onItem().transformToUni(result -> Uni.createFrom().voidItem())
                .onItem().transform(ignore -> Response.status(Response.Status.OK).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                });
    }

}
