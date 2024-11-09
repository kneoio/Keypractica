package io.kneo.qtracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.rls.RLSRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.qtracker.model.Owner;
import io.kneo.qtracker.repository.table.QTrackerNameResolver;
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
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import static io.kneo.qtracker.repository.table.QTrackerNameResolver.OWNERS;

@ApplicationScoped
public class OwnerRepository extends AsyncRepository {
    private static final EntityData entityData = QTrackerNameResolver.create().getEntityNames(OWNERS);

    @Inject
    public OwnerRepository(PgPool client, ObjectMapper mapper, RLSRepository rlsRepository) {
        super(client, mapper, rlsRepository);
    }

    public Uni<List<Owner>> getAll(final int limit, final int offset, final IUser user) {
        String sql = "SELECT * FROM " + entityData.getTableName() + " o, " + entityData.getRlsName() + " orr " +
                "WHERE o.id = orr.entity_id AND orr.reader = " + user.getId();
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Integer> getAllCount(IUser user) {
        return getAllCount(user.getId(), entityData.getTableName(), entityData.getRlsName());
    }

    public Uni<Owner> findById(UUID uuid, Long userID) {
        return client.preparedQuery(String.format("SELECT theTable.*, rls.* FROM %s theTable JOIN %s rls ON theTable.id = rls.entity_id " +
                        "WHERE rls.reader = $1 AND theTable.id = $2", entityData.getTableName(), entityData.getRlsName()))
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        LOGGER.warn(String.format("No %s found with id: " + uuid, entityData.getTableName()));
                        throw new DocumentHasNotFoundException(uuid);
                    }
                });
    }

    public Uni<Owner> insert(Owner doc, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();

        String sql = String.format("INSERT INTO %s " +
                "(reg_date, author, last_mod_date, last_mod_user, email, telegram_name, whatsapp_name, loc_name, phone, country, currency, status) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12) RETURNING id;", entityData.getTableName());

        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
        Tuple params = Tuple.of(
                nowTime,
                user.getId(),
                nowTime,
                user.getId()
                );

        params.addString(
                doc.getEmail()).
                addString(doc.getTelegramName()).
                addString(doc.getWhatsappName()).
                addJsonObject(localizedNameJson).
                addString(doc.getPhone()).
                addString(doc.getCountry()).
                addString(doc.getCurrency()).
                addInteger(doc.getStatus());

        String readersSql = String.format("INSERT INTO %s(reader, entity_id, can_edit, can_delete) VALUES($1, $2, $3, $4)", entityData.getRlsName());

        return client.withTransaction(tx -> {
            return tx.preparedQuery(sql)
                    .execute(params)
                    .onItem().transform(result -> result.iterator().next().getUUID("id"))
                    .onFailure().recoverWithUni(t -> Uni.createFrom().failure(t))
                    .onItem().transformToUni(id -> {
                        return tx.preparedQuery(readersSql)
                                .execute(Tuple.of(user.getId(), id, true, true))
                                .onItem().ignore().andContinueWithNull()
                                .onFailure().recoverWithUni(t -> Uni.createFrom().failure(t))
                                .onItem().transform(unused -> id);
                    });
        }).onItem().transformToUni(id -> findById(id, user.getId()));
    }

    public Uni<Owner> update(UUID id, Owner doc, IUser user) {
        return rlsRepository.findById(entityData.getRlsName(), user.getId(), id)
                .onItem().transformToUni(permissions -> {
                    if (permissions[0]) {
                        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();

                        String sql = String.format("UPDATE %s SET last_mod_user=$1, last_mod_date=$2, email=$3, telegram_name=$4," +
                                "telegram_name=$5, loc_name=$6, phone=$7, country=$8, currency=$9, status=$10 WHERE id=$11;", entityData.getTableName());

                        Tuple params = Tuple.of(
                                user.getId(),
                                nowTime
                        );
                        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
                        params.addString(doc.getEmail()).
                                addString(doc.getTelegramName()).
                                addString(doc.getWhatsappName()).
                                addJsonObject(localizedNameJson).
                                addString(doc.getPhone()).
                                addString(doc.getCountry()).
                                addString(doc.getCurrency()).
                                addInteger(doc.getStatus()).
                                addUUID(id);


                        return client.withTransaction(tx -> tx.preparedQuery(sql)
                                .execute(params)
                                .onItem().transformToUni(rowSet -> {
                                    if (rowSet.rowCount() == 0) {
                                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                                    }
                                    return findById(id, user.getId());
                                })
                                .onFailure().recoverWithUni(t -> Uni.createFrom().failure(t)));
                    } else {
                        return Uni.createFrom().failure(new DocumentModificationAccessException("User does not have edit permission", user.getUserName(), id));
                    }
                });
    }

    public Uni<Integer> delete(UUID uuid, IUser user) {
        return delete(uuid, entityData, user);
    }

    private Owner from(Row row) {
        Owner doc = new Owner();
        setDefaultFields(doc, row);
        doc.setEmail(row.getString("email"));
        doc.setTelegramName(row.getString("telegram_name"));
        doc.setWhatsappName(row.getString("whatsapp_name"));
        doc.setPhone(row.getString("phone"));
        doc.setCountry(row.getString("country"));
        doc.setCurrency(row.getString("currency"));
        JsonObject localizedNameJson = row.getJsonObject(COLUMN_LOCALIZED_NAME);
        if (localizedNameJson != null) {
            EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
            localizedNameJson.getMap().forEach((key, value) -> localizedName.put(LanguageCode.valueOf(key), (String) value));
            doc.setLocalizedName(localizedName);
        }

        return doc;
    }

    public Uni<Owner> findByTelegramId(String id, Long userId) {
        return client.preparedQuery(String.format("SELECT theTable.*, rls.* FROM %s theTable JOIN %s rls ON theTable.id = rls.entity_id " +
                        "WHERE rls.reader = $1 AND theTable.telegram_name = $2", entityData.getTableName(), entityData.getRlsName()))
                .execute(Tuple.of(userId, id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        LOGGER.warn(String.format("No %s found with telegram id: " + id, entityData.getTableName()));
                        throw new DocumentHasNotFoundException(id);
                    }
                });
    }
}
