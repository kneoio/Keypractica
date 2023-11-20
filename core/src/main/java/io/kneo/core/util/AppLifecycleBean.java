package io.kneo.core.util;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;


@ApplicationScoped
public class AppLifecycleBean {

    @Inject
    PgPool client;

    public void onStart(@Observes StartupEvent ev) {
        // simple query to check the database connectivity
        Uni<String> connected = client.query("SELECT 1")
                .execute()
                .onItem()
                .transform(rows -> "Database connected")
                .onFailure()
                .recoverWithItem("Database connection failed");

        connected.await().indefinitely(); // block until the result is received
    }
}
