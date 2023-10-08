package com.semantyca.core.repository;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class DatabaseResource implements QuarkusTestResourceLifecycleManager {

    private static final PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:13");

    @Override
    public Map<String, String> start() {
        db.start();

        Flyway flyway = Flyway.configure()
                .dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword())
                .load();
        flyway.migrate();


        Map<String, String> props = new HashMap<>();
        props.put("quarkus.datasource.reactive.url", db.getJdbcUrl().replace("jdbc:", ""));
        props.put("quarkus.datasource.url", db.getJdbcUrl());
        return props;
    }

    @Override
    public void stop() {
        if (db != null) {
            db.stop();
        }
    }

}
