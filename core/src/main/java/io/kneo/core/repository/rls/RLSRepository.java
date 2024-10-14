package io.kneo.core.repository.rls;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

@ApplicationScoped
public class RLSRepository {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final PgPool client;

    @Inject
    public RLSRepository(PgPool client) {
        this.client = client;
    }

    public Uni<boolean[]> findById(String accessTableName, Long userID, UUID uuid) {
        return client.preparedQuery("SELECT can_edit, can_delete FROM " + accessTableName + " a WHERE a.reader = $1 AND a.entity_id = $2")
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        boolean[] accessSet = from(iterator.next());
                        LOGGER.debug(Arrays.toString(accessSet));
                        return accessSet ;
                    } else {
                        LOGGER.warn(String.format("No %s found with id: " + uuid, accessTableName));
                        return new boolean[2];
                    }
                });
    }

    private boolean[] from(Row row) {
        boolean[] access = new boolean[2];
        access[0] = row.getBoolean("can_edit");
        access[1] = row.getBoolean("can_delete");
        return access;
    }
}