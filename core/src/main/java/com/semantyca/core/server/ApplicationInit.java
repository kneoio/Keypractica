package com.semantyca.core.server;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class ApplicationInit {

    private static final Logger LOGGER = LoggerFactory.getLogger("ListenerBean");

    @Inject
    @ConfigProperty(name = "quarkus.datasource.reactive.url")
    String jdbcUrl;

    @Inject
    PgPool client;

    void onStart(@Observes StartupEvent ev)  {
        LOGGER.info("The application is starting...{}", EnvConst.APP_ID);
        if (EnvConst.DEV_MODE) {
            LOGGER.info(EnvConst.APP_ID + "'s dev mode enabled");
            LOGGER.info("Database: {}", jdbcUrl);
            Uni<String> connected = client.query("SELECT 1")
                    .execute()
                    .onItem()
                    .transform(rows ->  "Database connected ...")
                    .onFailure()
                    .recoverWithItem("Database connection failed");
            LOGGER.info(connected.await().indefinitely());

        }

    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("The application is stopping...");
    }



}
