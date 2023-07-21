package com.semantyca.core.repository;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncRepo {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    public PgPool client;

    public Uni<Integer> getAllCount(long userID, String mainTable, String aclTable) {
        String sql = String.format("SELECT count(m.id) FROM %s as m, %s as acl WHERE m.id = acl.entity_id AND acl.reader = $1", mainTable, aclTable);
        return client.preparedQuery(sql)
                .execute(Tuple.of(userID))
                .onItem().transform(rows -> rows.iterator().next().getInteger(0));
    }
}
