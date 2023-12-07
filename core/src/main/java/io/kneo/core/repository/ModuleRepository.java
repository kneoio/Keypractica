package io.kneo.core.repository;

import io.kneo.core.model.Module;
import io.kneo.core.model.cnst.ModuleType;
import io.kneo.core.server.EnvConst;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ModuleRepository extends AsyncRepository {

    private static final String TABLE_NAME = "_modules";
    private static final String ENTITY_NAME = "module";
    @Inject
    PgPool client;
    public Uni<List<Module>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM " + TABLE_NAME;
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> {
                    return new Module.Builder()
                            .setId(row.getUUID("id"))
                            .setAuthor(row.getLong("author"))
                            .setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()))
                            .setOn(row.getBoolean("is_on"))
                            .setLocalizedName(getLocalizedData(row.getJsonObject("loc_name")))
                            .setLocalizedDescription(getLocalizedData(row.getJsonObject("loc_descr")))
                            .setIdentifier(row.getString("identifier"))
                            .build();
                })
                .collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(TABLE_NAME);
    }

    public Uni<List<Module>> getModules(ModuleType[] defaultModules) {
        return client.query(String.format("SELECT * FROM _modules LIMIT %d OFFSET 0", EnvConst.DEFAULT_PAGE_SIZE))
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new Module.Builder().setIdentifier(row.getString("identifier")).build())
                .collect().asList();
    }

    public Uni<Optional<Module>> findById(UUID uuid) {
        return client.preparedQuery(String.format("SELECT * FROM %s WHERE id = $1", TABLE_NAME))
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ?  Optional.of(from(iterator.next())) : Optional.empty());
    }

    private Module from(Row row) {
        Module doc = new Module();
        setDefaultFields(doc, row);
        doc.setIdentifier(row.getString("identifier"));
        doc.setLocalizedName(extractLanguageMap(row));
        doc.setOn(row.getBoolean("is_on"));
        return doc;
    }

    public Uni<UUID> insert(Module doc, long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = "INSERT INTO _modules (author, is_on, identifier, reg_date, last_mod_date, last_mod_user, loc_name, loc_descr) VALUES($1, $2, $3, $4, $5, $6, $7, $8) RETURNING id";
        Tuple params = Tuple.of(user, doc.isOn(), doc.getIdentifier(), nowTime, nowTime, user);
        Tuple finalParams = params.addJsonObject(JsonObject.mapFrom(doc.getLocalizedName())).addJsonObject(JsonObject.mapFrom(doc.getLocalizedDescription()));
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(finalParams)
                .onItem().transform(result -> result.iterator().next().getUUID("id"))
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to insert to %s", ENTITY_NAME), throwable));
                }));
    }


    public Module update(Module doc) {

        return doc;
    }

    public Uni<Void> delete(UUID uuid) {
        return delete(uuid, TABLE_NAME);
    }


}
