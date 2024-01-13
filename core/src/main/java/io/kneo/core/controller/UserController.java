package io.kneo.core.controller;

import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.User;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.UserService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

import java.net.URI;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class UserController extends AbstractController<User, UserDTO> {
    @Inject
    UserService service;

    @GET
    @Path("/")
    public Uni<Response> get() {
        ViewPage viewPage = new ViewPage();
          return service.getAll().onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
                return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/search/{keyword}")
    public Uni<Response> search(@PathParam("keyword") String keyword) {
        ViewPage viewPage = new ViewPage();
        return service.search(keyword).onItem().transform(userList -> {
            viewPage.addPayload(PayloadType.VIEW_DATA, userList);
            return Response.ok(viewPage).build();
        });
    }

    @GET
    @Path("/stream")
    @Consumes(MediaType.SERVER_SENT_EVENTS)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<IUser> getStream() {
        return service.getAllStream();
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id) {
        FormPage page = new FormPage();
        page.addPayload(PayloadType.CONTEXT_ACTIONS, new ContextAction());
        return service.get(id).onItem().transform(userOptional -> {
            userOptional.ifPresentOrElse(user ->  page.addPayload(PayloadType.DOC_DATA, user),
                    () ->  page.addPayload(PayloadType.DOC_DATA, "no_data"));
            return Response.ok(page).build();
        });
    }

    @POST
    @Path("/")
    public Uni<Response> create(@Valid UserDTO userDTO) {
        return service.add(userDTO)
                .onItem().transform(id -> Response.status(Response.Status.CREATED).build())
                .onFailure().recoverWithItem(e -> {
                    LOGGER.error(e.getMessage(), e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
                });

    }

    @PUT
    @Path("/")
    public Response update(@Valid UserDTO userDTO) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(userDTO))).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWord(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
