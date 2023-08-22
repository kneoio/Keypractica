package com.semantyca.core.controller;

import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.cnst.PayloadType;
import com.semantyca.core.dto.document.UserDTO;
import com.semantyca.core.dto.document.UserRegistrationDTO;
import com.semantyca.core.dto.form.FormPage;
import com.semantyca.core.dto.view.ViewOptionsFactory;
import com.semantyca.core.dto.view.ViewPage;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.repository.exception.DocumentModificationAccessException;
import com.semantyca.core.service.RegistrationService;
import com.semantyca.core.service.UserService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

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

    @POST
    @Path("/register")
    public Response register(@Valid UserRegistrationDTO userRegistration) {
        String token = registrationService.register(userRegistration);
        return Response.ok().entity(token).build();
    }

    @POST
    @Path("/")
    public Response create(UserDTO userDTO) {
        return Response.created(URI.create("/" + service.add(userDTO))).build();
    }

    @PUT
    @Path("/")
    public Response update(UserDTO userDTO) throws DocumentModificationAccessException {
        return Response.ok(URI.create("/" + service.update(userDTO).getId())).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteWord(@PathParam("id") String id) {
        return Response.ok().build();
    }

}
