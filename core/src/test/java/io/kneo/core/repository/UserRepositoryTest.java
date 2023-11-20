package io.kneo.core.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(DatabaseResource.class)
class UserRepositoryTest {

    @Inject
    PgPool client;


    @Test
    public void testConnection() {
        Assertions.assertNotNull(client);
    }

    @Test
    void insert() {
    }


}