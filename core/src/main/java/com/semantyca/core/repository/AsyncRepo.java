package com.semantyca.core.repository;

import com.semantyca.core.model.embedded.RLS;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public Uni<List<RLS>> getAllReaders(UUID uuid) {
        return client.preparedQuery("SELECT reader, reading_time, can_edit, can_delete FROM prj__tasks p, prj__task_readers ppr WHERE p.id = ppr.entity_id AND p.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new RLS(
                        Optional.ofNullable(row.getLocalDateTime("reading_time"))
                                .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault())))
                                .orElse(null),
                        row.getLong("reader"),
                        row.getLong("can_edit"),
                        row.getLong("can_delete")))
                .collect().asList();
    }
}
