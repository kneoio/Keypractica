package io.kneo.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Language;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.core.repository.cnst.Tables.LANGUAGES_TABLE_NAME;
import static io.kneo.core.repository.table.TableNameResolver.LANGUAGE_ENTITY_NAME;

@ApplicationScoped
public class LanguageRepository extends AsyncRepository {
    private static final EntityData entityData = TableNameResolver.create().getEntityNames(LANGUAGE_ENTITY_NAME);

    @Inject
    public LanguageRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }


    public Uni<List<Language>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM _langs";
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }

        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(entityData.getTableName());
    }

    public Uni<List<Language>> getAvailable() {
        String sql = "SELECT * FROM " + LANGUAGES_TABLE_NAME + " WHERE is_on = 'true'";
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Language> findById(UUID uuid) {
        return client.preparedQuery("SELECT * FROM _langs WHERE id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> from(iterator.next()));
    }

    public Uni<Optional<Language>> findByCode(LanguageCode code) {
        return client.preparedQuery("SELECT * FROM _langs WHERE code = $1")
                .execute(Tuple.of(code))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private Language from(Row row) {
        Language doc = new Language();
        setDefaultFields(doc, row);
        doc.setCode(LanguageCode.valueOf(row.getString("code")));
        doc.setOn(row.getBoolean("is_on"));
        doc.setPosition(row.getInteger("position"));
        setLocalizedNames(doc, row);
        return doc;
    }


    public Uni<Language> insert(Language doc, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = "INSERT INTO _langs (author, code, reg_date, position, last_mod_date, last_mod_user, loc_name, is_on) VALUES($1, $2, $3, $4, $5, $6, $7, $8) RETURNING id";
        Tuple params = Tuple.of(user.getId(), doc.getCode(), nowTime, doc.getPosition(), nowTime, user.getId());
        Tuple finalParams = params.addJsonObject(JsonObject.mapFrom(doc.getLocalizedName())).addBoolean(doc.isOn());

        return client.preparedQuery(sql)
                .execute(finalParams)
                .onItem().transformToUni(result -> {
                    UUID id = result.iterator().next().getUUID("id");
                    return findById(id).onItem()
                            .transform(optionalOrg -> optionalOrg);
                });
    }

    public Uni<Language> update(UUID id, Language doc, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = "UPDATE _langs SET code=$1, position=$2, last_mod_date=$3, last_mod_user=$4, is_on=$5, loc_name=$6 WHERE id=$7";
        Tuple params = Tuple.of(doc.getCode(), doc.getPosition(), nowTime, user.getId(), doc.isOn(), JsonObject.mapFrom(doc.getLocalizedName()));
        Tuple finalParams = params.addUUID(id);
        return client.preparedQuery(sql)
                .execute(finalParams)
                .onItem().transformToUni(rowSet -> {
                    if (rowSet.rowCount() == 0) {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                    }
                    return findById(id);
                })
                .onItem().transform(entity -> entity);
    }

    public Uni<Integer> delete(UUID uuid) {
        return delete(uuid, entityData);
    }


}
