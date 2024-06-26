package io.kneo.officeframe.controller;

import io.kneo.core.controller.AbstractSecuredController;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.PayloadType;
import io.kneo.core.dto.form.FormPage;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.UserNotFoundException;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.service.PositionService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("**")
public class PositionController extends AbstractSecuredController<Position, PositionDTO> {
    @Inject
    PositionService service;

    public PositionController(UserService userService) {
        super(userService);
    }

    @GET
    @Path("/")
    @PermitAll
    public Uni<Response> get(@Valid @Min(0) @QueryParam("page") int page, @Valid @Min(0) @QueryParam("size") int size, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        return getAll(service, requestContext, page, size);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") String id, @Context ContainerRequestContext requestContext) throws UserNotFoundException {
        Optional<IUser> userOptional = getUserId(requestContext);
        if (userOptional.isPresent()) {
            IUser user = userOptional.get();
            FormPage page = new FormPage();
            page.addPayload(PayloadType.CONTEXT_ACTIONS, new ActionBox());
            return service.getDTO(id, user, LanguageCode.ENG)
                    .onItem().transform(p -> {
                        page.addPayload(PayloadType.DOC_DATA, p);
                        return Response.ok(page).build();
                    })
                    .onFailure().recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        } else {
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
