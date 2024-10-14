package io.kneo.core.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.DataEntity;
import io.kneo.core.model.SimpleReferenceEntity;
import io.kneo.core.model.embedded.RLS;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.rls.RLSRepository;
import io.kneo.core.repository.table.EntityData;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsyncRepository {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());
    protected static final String COLUMN_AUTHOR = "author";
    protected static final String COLUMN_REG_DATE = "reg_date";
    protected static final String COLUMN_LAST_MOD_DATE = "last_mod_date";
    protected static final String COLUMN_LAST_MOD_USER = "last_mod_user";
    protected static final String COLUMN_IDENTIFIER = "identifier";
    protected static final String COLUMN_RANK = "rank";
    protected static final String COLUMN_LOCALIZED_NAME = "loc_name";

    protected PgPool client;
    protected ObjectMapper mapper;
    protected RLSRepository rlsRepository;

    public AsyncRepository() {

    }

    public AsyncRepository(PgPool client, ObjectMapper mapper, RLSRepository rlsRepository) {
        this.client = client;
        this.mapper = mapper;
        this.rlsRepository = rlsRepository;
    }

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

    public <R> Uni<R> findById(UUID uuid, EntityData entityData, Function<Row, R> fromFunc) {
        return client.preparedQuery("SELECT * FROM " + entityData.getTableName() + " se WHERE se.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transformToUni(rowSet -> {
                    var iterator = rowSet.iterator();
                    if (iterator.hasNext()) {
                        return Uni.createFrom().item(fromFunc.apply(iterator.next()));
                    } else {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(uuid));
                    }
                });
    }

    public <R> Uni<R> findByIdentifier(String identifier, EntityData entityData, Function<Row, R> fromFunc) {
        return client.preparedQuery("SELECT * FROM " + entityData.getTableName() + " t WHERE t.identifier = $1")
                .execute(Tuple.of(identifier))
                .onItem().transformToUni(rowSet -> {
                    var iterator = rowSet.iterator();
                    if (iterator.hasNext()) {
                        return Uni.createFrom().item(fromFunc.apply(iterator.next()));
                    } else {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(entityData.getTableName() + " " + identifier));
                    }
                });
    }

    public Uni<List<RLS>> getAllReaders(UUID uuid, EntityData entityData) {
        String sql = String.format("SELECT reader, reading_time, can_edit, can_delete FROM %s t, %s rls WHERE t.id = rls.entity_id AND t.id = $1", entityData.getTableName(), entityData.getRlsName());
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

    protected Uni<Integer> delete(UUID uuid, EntityData entityData) {
        String sql = String.format("DELETE FROM %s WHERE id = $1", entityData.getTableName());
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(Tuple.of(uuid))
                .onItem().transformToUni(rowSet -> {
                    int rowCount = rowSet.rowCount();
                    if (rowCount == 0) {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(uuid));
                    }
                    return Uni.createFrom().item(rowCount);
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to delete %s", entityData.getTableName()), throwable));
                }));
    }

    public Uni<Integer> delete(UUID id, EntityData entityData, IUser user) {
        return rlsRepository.findById(entityData.getRlsName(), user.getId(), id)
                .onItem().transformToUni(permissions -> {
                    if (permissions[1]) {
                        String sql = String.format("DELETE FROM %s WHERE id=$1;", entityData.getTableName());
                        return client.withTransaction(tx -> tx.preparedQuery(sql)
                                .execute(Tuple.of(id))
                                .onItem().transformToUni(rowSet -> {
                                    int rowCount = rowSet.rowCount();
                                    if (rowCount == 0) {
                                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                                    }
                                    return Uni.createFrom().item(rowCount);
                                })
                                .onFailure().recoverWithUni(t ->
                                        Uni.createFrom().failure(t)));

                    } else {
                        return Uni.createFrom().failure(new DocumentModificationAccessException("User does not have delete permission", user.getUserName(), id));
                    }
                });
    }

    protected static void setDefaultFields(DataEntity<UUID> entity, Row row) {
        entity.setId(row.getUUID("id"));
        entity.setAuthor(row.getLong(COLUMN_AUTHOR));
        entity.setRegDate(row.getLocalDateTime(COLUMN_REG_DATE).atZone(ZoneId.systemDefault()));
        entity.setLastModifier(row.getLong(COLUMN_LAST_MOD_USER));
        entity.setLastModifiedDate(row.getLocalDateTime(COLUMN_LAST_MOD_DATE).atZone(ZoneId.systemDefault()));
    }

    protected static void setLocalizedNames(SimpleReferenceEntity entity, Row row) {
        setLocalizedNames(entity, row, COLUMN_LOCALIZED_NAME);
        JsonObject localizedNameJson = row.getJsonObject(COLUMN_LOCALIZED_NAME);
        if (localizedNameJson != null) {
            EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
            localizedNameJson.getMap().forEach((key, value) -> localizedName.put(LanguageCode.valueOf(key), (String) value));
            entity.setLocalizedName(localizedName);
        }
    }

    protected static void setLocalizedNames(SimpleReferenceEntity entity, Row row, String fieldName) {
        JsonObject localizedNameJson = row.getJsonObject(COLUMN_LOCALIZED_NAME);
        if (localizedNameJson != null) {
            EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
            localizedNameJson.getMap().forEach((key, value) -> localizedName.put(LanguageCode.valueOf(key), (String) value));
            entity.setLocalizedName(localizedName);
        }
    }

    @Deprecated
    protected EnumMap<LanguageCode, String> extractLanguageMap(Row row) {
        EnumMap<LanguageCode, String> map;
        try {
            map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return map;
    }

    @Deprecated
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

    @Deprecated
    protected EnumMap<LanguageCode, String> getLocalizedNameFromDb(Row row) {
        try {
            JsonObject localizedNameJson = row.getJsonObject(COLUMN_LOCALIZED_NAME);
            return convertToEnumMap(localizedNameJson.getMap());
        } catch (Exception e) {
            return new EnumMap<>(LanguageCode.class);
        }
    }

    private static EnumMap<LanguageCode, String> convertToEnumMap(Map<String, Object> linkedHashMap) {
        EnumMap<LanguageCode, String> enumMap = new EnumMap<>(LanguageCode.class);

        for (Map.Entry<String, Object> entry : linkedHashMap.entrySet()) {
            try {
                LanguageCode key = LanguageCode.valueOf(entry.getKey().toUpperCase());
                enumMap.put(key, (String) entry.getValue());
            } catch (IllegalArgumentException e) {
                enumMap.put(LanguageCode.UNKNOWN, (String) entry.getValue());
            }
        }

        return enumMap;
    }
}