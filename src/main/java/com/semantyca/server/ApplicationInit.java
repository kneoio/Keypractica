package com.semantyca.server;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.IOException;
import java.sql.SQLException;

@ApplicationScoped
public class ApplicationInit {

    private static final Logger LOGGER = LoggerFactory.getLogger("ListenerBean");

    public ApplicationInit(Jdbi jdbi) {
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new PostgresPlugin());
        jdbi.installPlugin(new Jackson2Plugin());
        //jdbi.getConfig(Arguments.class).register(new LocalizedNamesArgumentFactory());
       // jdbi.installPlugin(new JsonPlugin());

    }


    void onStart(@Observes StartupEvent ev) throws SQLException, IOException {
        LOGGER.info("The application is starting...{}", EnvConst.APP_ID);
        initdb();

    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("The application is stopping...");
    }


    private void initdb() {
        /*client.query("DROP TABLE IF EXISTS fruits").execute()
                .flatMap(r -> client.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Kiwi')").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Durian')").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Pomelo')").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Lychee')").execute())
                .await().indefinitely();*/
    }


}
