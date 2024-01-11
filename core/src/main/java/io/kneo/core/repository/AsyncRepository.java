package io.kneo.core.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.DataEntity;
import io.kneo.core.model.embedded.RLS;
import io.kneo.core.repository.table.EntityData;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsyncRepository {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    public PgPool client;
    @Inject
    ObjectMapper mapper;

    protected Uni<Integer> getAllCount(long userID, String mainTable, String aclTable) {
        String sql = String.format("SELECT count(m.id) FROM %s as m, %s as acl WHERE m.id = acl.entity_id AND acl.reader = $1", mainTable, aclTable);
        return client.preparedQuery(sql)
                .execute(Tuple.of(userID))
                .onItem().transform(rows -> rows.iterator().next().getInteger(0));
    }

    public Uni<Integer> getAllCount(String mainTable) {
        String sql = String.format("SELECT count(m.id) FROM %s as m", mainTable);
        return client.preparedQuery(sql)
                .execute()
                .onItem().transform(rows -> rows.iterator().next().getInteger(0));
    }
    public <R> Uni<Optional<R>> findById(UUID uuid, EntityData entityData, Function<Row, R> fromFunc) {
        return client.preparedQuery("SELECT * FROM " + entityData.tableName() + " se WHERE se.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(fromFunc.apply(iterator.next())) : Optional.empty());
    }

    public Uni<List<RLS>> getAllReaders(UUID uuid, EntityData entityData) {
        String sql = String.format("SELECT reader, reading_time, can_edit, can_delete FROM %s t, %s rls WHERE t.id = rls.entity_id AND t.id = $1", entityData.tableName(), entityData.rlsName());
        return client.preparedQuery(sql)
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


    protected Uni<Void> delete(UUID uuid, String table) {
        String sql = String.format("DELETE FROM %s WHERE id = $1", table);
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(Tuple.of(uuid))
                .onItem().ignore().andContinueWithNull()
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to delete %s", table), throwable));
                }));
    }

    protected  static void setDefaultFields(DataEntity<UUID> entity, Row row) {
        entity.setId(row.getUUID("id"));
        entity.setAuthor(row.getLong("author"));
        entity.setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()));
        entity.setLastModifier(row.getLong("last_mod_user"));
        entity.setLastModifiedDate(row.getLocalDateTime("last_mod_date").atZone(ZoneId.systemDefault()));
    }

    protected EnumMap<LanguageCode, String> extractLanguageMap(Row row) {
        EnumMap<LanguageCode, String> map;
        try {
            map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {
            });
        } catch (
                JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return map;
    }

    protected static EnumMap<LanguageCode, String> getLocalizedData(JsonObject json) {
        if (json != null) {
            Map<LanguageCode, String> map = json.getMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> LanguageCode.valueOf(entry.getKey()),
                            entry -> String.valueOf(entry.getValue()),
                            (existing, replacement) -> existing));
            if (!map.isEmpty()) {
                return new EnumMap<>(map);
            }
        }
        return new EnumMap<>(LanguageCode.class);
    }


    protected static String getBaseSelect(String baseRequest, final int limit, final int offset) {
        String sql = baseRequest;
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return sql;
    }
}
