package io.kneo.core.controller;

import io.kneo.core.dto.actions.ActionBar;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.dto.document.UserRegistrationDTO;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.dto.view.ViewOptionsFactory;
import io.kneo.core.dto.view.ViewPage;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.User;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.RegistrationService;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class UserController extends AbstractController<User, UserDTO> {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    UserService service;

    @Inject
    RegistrationService registrationService;

    @GET
    @Path("/")
    public Uni<Response> get() {
        ViewPage viewPage = new ViewPage();
        viewPage.addPayload(PayloadType.ACTIONS, ViewOptionsFactory.getProjectOptions());
        return service.getAll().onItem().transform(userList -> {
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
        page.addPayload(PayloadType.ACTIONS, new ActionBar());
        return service.get(id).onItem().transform(userOptional -> {
            userOptional.ifPresentOrElse(user ->  page.addPayload(PayloadType.FORM_DATA, user),
                    () ->  page.addPayload(PayloadType.FORM_DATA, "no_data"));
            return Response.ok(page).build();
        });
    }

    @GET
    @Path("/register")
    public Response register(@PathParam("confirmation") String confirmation) {
        String token = registrationService.confirmation(confirmation);
        return Response.ok().entity(token).build();
    }
    @POST
    @Path("/register")
    public Response register(@Valid UserRegistrationDTO userRegistration) {
        String token = registrationService.register(userRegistration);
        return Response.ok().entity(token).build();
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
