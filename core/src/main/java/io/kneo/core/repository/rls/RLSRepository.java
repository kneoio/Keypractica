package io.kneo.core.repository.rls;

import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@ApplicationScoped
public class RLSRepository {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Inject
    PgPool client;

    public int[] findById(String accessTableName, Long userID, UUID uuid) {
        return client.preparedQuery("SELECT can_edit, can_delete FROM " + accessTableName + " WHERE ptr.reader = $1 AND pt.entity_id = $2")
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        LOGGER.warn(String.format("No %s found with id: " + uuid, accessTableName));
                        return new int[2];
                    }
                })
                .await().indefinitely();
    }


    private int[] from(Row row) {
        int[] access = new int[2];
        access[0] = row.getInteger("can_edit");
        access[1] = row.getInteger("can_delete");
        return access;
    }
}
