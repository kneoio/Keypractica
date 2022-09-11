package com.semantyca.controller;


import com.semantyca.dto.IPage;
import com.semantyca.server.EnvConst;
import com.semantyca.service.RunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@Singleton
@Path("/run")
public class RunController {

    @Inject
    private RunService runService;

    private static final Logger LOGGER = LoggerFactory.getLogger("RunController");

    @GET
    @Path("/{id}/info")
    public Response get(@Context SecurityContext ctx)  {
        LOGGER.info("context=" + ctx.getUserPrincipal());
        return Response.ok("version:" + EnvConst.VERSION).build();
    }

    @POST
    @Path("/presentation/{id}/{command}")
    public Response start(@PathParam("id") String id, @PathParam("command") String command) throws URISyntaxException, IOException {
        IPage page = runService.start(id);
        if (page != null) {
            return Response.ok().entity( Map.of("page", page)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


}
