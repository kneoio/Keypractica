package com.semantyca.server;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class ApplicationInit {

    private static final Logger LOGGER = LoggerFactory.getLogger("ListenerBean");



    void onStart(@Observes StartupEvent ev)  {
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
        //CREATE CONSTRAINT unique_language_code FOR (n:Language) REQUIRE n.code IS UNIQUE
    }


}
