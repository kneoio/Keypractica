
package io.kneo.core.controller;

import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.View;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.Language;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.LanguageService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

@Path("/languages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class LanguageController extends AbstractSecuredController<Language, LanguageDTO> {
    @Inject
    LanguageService service;

    @GET
    @Path("/")
    public Response get()  {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
        View<LanguageDTO> view = new View<>(service.getAll(100, 0).await().indefinitely());
        viewPage.addPayload(PayloadType.VIEW_DATA, view);
        return Response.ok(viewPage).build();
    }

    @GET
    @Path("/code/{code}")
    public Response getByCode(@PathParam("code") String code)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
        page.addPayload(PayloadType.FORM_DATA, service.findByCode(code.toUpperCase()).await().indefinitely());
        return Response.ok(page).build();
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id)  {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
        return service.getDTO(id, AnonymousUser.build())
                .onItem().transform(p -> {
                    page.addPayload(PayloadType.FORM_DATA, p);
                    return Response.ok(page).build();
                })
                .onFailure().invoke(failure -> LOGGER.error(failure.getMessage()))
                .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build());
    }

    @POST
    public Uni<Response> create(LanguageDTO dto, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        return create(service, dto, requestContext);
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(LanguageDTO dto, @PathParam("id") String id, @Context ContainerRequestContext requestContext) throws UserNotFoundException, DocumentModificationAccessException {
        return update(service, dto, requestContext);
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
