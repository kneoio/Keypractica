package com.semantyca.core.repository;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class DatabaseResource implements QuarkusTestResourceLifecycleManager {

    private static final PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:9.6.8");

    @Override
    public Map<String, String> start() {
        db.start();
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
