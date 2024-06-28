package io.kneo.core.server;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ClientContextFilter {
    private static final Logger LOGGER = Logger.getLogger(ClientContextFilter.class);

    @RouteFilter(100)
    void filter(RoutingContext rc) {
        String path = rc.request().uri();
        if (path.startsWith("/api/")) {

            String[] parts = path.split("/", 4);
            if (parts.length >= 4) {
                String originalPath = rc.request().path();
                System.out.println("request: " + originalPath);
                rc.put("client_database", "postgresql://localhost:5433/flabspoema2");
                rc.next();
            }
        }
    }
}
