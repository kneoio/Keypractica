package com.semantyca.core.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;

@Path("/tests")
public class TestController {

    @GET
    @Path("/{id}")
    public void getById(@PathParam("id") String id, @Suspended final AsyncResponse asyncResponse) {
        // simulate a long running operation
        new Thread(() -> {
            try {
                Thread.sleep(5000); // delay for 5 seconds
                asyncResponse.resume("Project details for id: " + id);
            } catch (InterruptedException e) {
                asyncResponse.resume(e);
            }
        }).start();
    }
}
