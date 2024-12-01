package io.kneo.core.server;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientContextFilter {

    String clientDatabaseUrl = "test";

    @RouteFilter(100)
    void filter(RoutingContext rc) {
        String path = rc.request().uri();
        if (path.startsWith("/api/")) {
            String[] parts = path.split("/", 4);
            if (parts.length >= 4) {
                rc.put("client_database", clientDatabaseUrl);
                rc.next();
            }
        } else {
            rc.next();
        }
    }
}
