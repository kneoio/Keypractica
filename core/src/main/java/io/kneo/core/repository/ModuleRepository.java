package io.kneo.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.model.Module;
import io.kneo.core.model.UserModule;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.cnst.Tables;
import io.kneo.core.repository.table.EntityData;
import io.kneo.core.repository.table.TableNameResolver;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.core.repository.cnst.Tables.*;

@ApplicationScoped
public class ModuleRepository extends AsyncRepository {
    private static final EntityData entityData = TableNameResolver.create().getEntityNames(MODULES_ENTITY_NAME);

    @Inject
    public ModuleRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }

    public Uni<List<Module>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM " + MODULES_TABLE_NAME;
        if (limit > 0) {
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
        return getAllCount(MODULES_TABLE_NAME);
    }

    public Uni<List<UserModule>> getAvailable(IUser user) {
        String sql;
        Tuple params;
        if (user.getId() == AnonymousUser.ID) {
            sql = String.format("SELECT 0 as position, 'classic' as theme, false as invisible, identifier, loc_name, loc_descr " +
                    "FROM %s m WHERE m.is_on='true' AND m.is_public = $1 " +
                    "LIMIT 50 OFFSET 0", MODULES_TABLE_NAME);
            params = Tuple.of(true);
        } else {
            sql = String.format("SELECT position, theme, invisible, identifier, loc_name, loc_descr " +
                    "FROM %s um, %s m " +
                    "WHERE um.module_id = m.id AND m.is_on='true' AND um.is_on = 'true' AND um.invisible = 'false' AND um.user_id = $1 " +
                    "LIMIT 50 OFFSET 0", USER_MODULES_TABLE_NAME, MODULES_TABLE_NAME);
            params = Tuple.of(user.getId());
        }

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> {
                    UserModule doc = new UserModule();
                    doc.setPosition(row.getInteger("position"));
                    doc.setTheme(row.getString("theme"));
                    doc.setInvisible(row.getBoolean("invisible"));
                    doc.setIdentifier(row.getString("identifier"));
                    doc.setLocalizedName(getLocalizedData(row.getJsonObject("loc_name")));
                    doc.setLocalizedDescription(getLocalizedData(row.getJsonObject("loc_descr")));
                    return doc;
                })
                .collect().asList();
    }

    public Uni<List<Optional<Module>>> getModules(String[] defaultModules) {
        String sql = "SELECT * FROM " + MODULES_TABLE_NAME;
        if (defaultModules != null && defaultModules.length > 0) {
            String inClause = Arrays.stream(defaultModules)
                    .map(identifier -> "'" + identifier + "'")
                    .collect(Collectors.joining(", "));
            sql += " WHERE identifier IN (" + inClause + ")";
        }

        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> {
                    Optional<Module> module = Optional.ofNullable(new Module.Builder()
                            .setId(row.getUUID("id"))
                            .setAuthor(row.getLong("author"))
                            .setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()))
                            .setOn(row.getBoolean("is_on"))
                            .setLocalizedName(getLocalizedData(row.getJsonObject("loc_name")))
                            .setLocalizedDescription(getLocalizedData(row.getJsonObject("loc_descr")))
                            .setIdentifier(row.getString("identifier"))
                            .build());
                    return module;
                })
                .collect().asList();
    }

    public Uni<Optional<Module>> findById(UUID uuid) {
        return client.preparedQuery(String.format("SELECT * FROM %s WHERE id = $1", MODULES_TABLE_NAME))
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
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
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to insert to %s", Tables.MODULES_ENTITY_NAME), throwable));
                }));
    }


    public Module update(Module doc) {

        return doc;
    }

    public Uni<Integer> delete(UUID uuid) {
        return delete(uuid, entityData);
    }


}
