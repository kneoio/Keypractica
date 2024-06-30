package io.kneo.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Language;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.core.repository.cnst.Tables.LANGUAGES_TABLE_NAME;

@ApplicationScoped
public class LanguageRepository extends AsyncRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger("LanguageRepository");

    @Inject
    public LanguageRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper);
    }


    public Uni<List<Language>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM _langs";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }

        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<List<Language>> getAvailable() {
        String sql = "SELECT * FROM " + LANGUAGES_TABLE_NAME + " WHERE is_on = 'true'";
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Optional<Language>> findById(UUID uuid) {
        return client.preparedQuery("SELECT * FROM _langs WHERE id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ?  Optional.of(from(iterator.next())) : Optional.empty());
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
        doc.setLocalizedName(extractLanguageMap(row));
        doc.setOn(row.getBoolean("is_on"));
        doc.setPosition(row.getInteger("position"));
        return doc;
    }


    public Uni<UUID> insert(Language doc, Long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = "INSERT INTO _langs (author, code, reg_date, position, last_mod_date, last_mod_user, loc_name, is_on) VALUES($1, $2, $3, $4, $5, $6, $7, $8) RETURNING id";
        Tuple params = Tuple.of(user, doc.getCode(), nowTime, doc.getPosition(), nowTime, user);
        Tuple finalParams = params.addJsonObject(JsonObject.mapFrom(doc.getLocalizedName())).addBoolean(doc.isOn());

        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(finalParams)
                .onItem().transform(result -> result.iterator().next().getUUID("id"))
           .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException("Failed to insert language", throwable));
                }));
    }

    public Uni<Integer> update(Language doc, long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = "UPDATE _langs SET code=$1, position=$2, last_mod_date=$3, last_mod_user=$4, is_on=$5, loc_name=$6 WHERE id=$7";
        Tuple params = Tuple.of(doc.getCode(), doc.getPosition(), nowTime, user, doc.isOn(), JsonObject.mapFrom(doc.getLocalizedName()));
        Tuple finalParams = params.addUUID(doc.getId());
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(finalParams)
                .onItem().transform(result -> result.rowCount() > 0 ? 1 : 0)
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().item(0);
                }));
    }

    public Uni<Void> delete(UUID uuid) {
       return delete(uuid, "_langs");
    }


}
