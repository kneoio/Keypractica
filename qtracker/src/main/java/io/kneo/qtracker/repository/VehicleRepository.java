package io.kneo.qtracker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.rls.RLSRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.qtracker.model.Vehicle;
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

@ApplicationScoped
public class VehicleRepository extends AsyncRepository {
    private static final EntityData entityData = QTrackerNameResolver.create().getEntityNames(QTrackerNameResolver.VEHICLES);

    @Inject
    public VehicleRepository(PgPool client, ObjectMapper mapper, RLSRepository rlsRepository) {
        super(client, mapper, rlsRepository);
    }

    public Uni<List<Vehicle>> getAll(final int limit, final int offset, final IUser user) {
        String sql = "SELECT * FROM " + entityData.getTableName() + " v, " + entityData.getRlsName() + " vr WHERE v.id = vr.entity_id AND vr.reader = $1";
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.preparedQuery(sql)
                .execute(Tuple.of(user.getId()))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Integer> getAllCount(IUser user) {
        return getAllCount(user.getId(), entityData.getTableName(), entityData.getRlsName());
    }

    public Uni<List<Vehicle>> getOwnedBy(final UUID ownerId,  final IUser user) {
        String sql = "SELECT * FROM " + entityData.getTableName() + " v, " + entityData.getRlsName() +
                " vr WHERE v.id = vr.entity_id AND v.owner_id = $1 AND vr.reader = $2";
        return client.preparedQuery(sql)
                .execute(Tuple.of(ownerId, user.getId()))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Vehicle> findById(UUID uuid, Long userID) {
        String sql = "SELECT theTable.*, rls.* FROM " + entityData.getTableName() + " theTable " +
                "JOIN " + entityData.getRlsName() + " rls ON theTable.id = rls.entity_id " +
                "WHERE rls.reader = $1 AND theTable.id = $2";

        return client.preparedQuery(sql)
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        LOGGER.warn(String.format("No %s found with id: " + uuid, entityData.getTableName()));
                        return null;
                    }
                });
    }


    public Uni<Vehicle> insert(Vehicle doc, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();

        String sql = String.format("INSERT INTO %s " +
                "(reg_date, author, last_mod_date, last_mod_user, vin, vehicle_type, brand, model, fuel_type, owner_id, status, loc_name) " +
                "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12) RETURNING id;", entityData.getTableName());

        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
        Tuple params = Tuple.tuple();
        params.addValue(nowTime)
                .addValue(user.getId())
                .addValue(nowTime)
                .addValue(user.getId())
                .addValue(doc.getVin())
                .addValue(doc.getVehicleType())
                .addValue(doc.getBrand())
                .addValue(doc.getModel())
                .addValue(doc.getFuelType())
                .addUUID(doc.getOwnerId())
                .addInteger(doc.getStatus())
                .addJsonObject(localizedNameJson);

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

    public Uni<Vehicle> update(UUID id, Vehicle doc, IUser user) {
        return rlsRepository.findById(entityData.getRlsName(), user.getId(), id)
                .onItem().transformToUni(permissions -> {
                    if (permissions[0]) {
                        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();

                        String sql = String.format("UPDATE %s SET last_mod_user=$1, last_mod_date=$2, vin=$3, " +
                                "vehicle_type=$4, brand=$5, model=$6, fuel_type=$7, owner_id=$8, status=$9, loc_name=$10 WHERE id=$11;", entityData.getTableName());

                        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
                        Tuple params = Tuple.tuple();
                        params.addValue(user.getId())
                                .addValue(nowTime)
                                .addValue(doc.getVin())
                                .addValue(doc.getVehicleType())
                                .addValue(doc.getBrand())
                                .addValue(doc.getModel())
                                .addValue(doc.getFuelType())
                                .addValue(doc.getOwnerId())
                                .addValue(doc.getStatus())
                                .addJsonObject(localizedNameJson)
                                .addValue(id);

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

    private Vehicle from(Row row) {
        Vehicle doc = new Vehicle();
        setDefaultFields(doc, row);
        doc.setVin(row.getString("vin"));
        doc.setVehicleType(row.getInteger("vehicle_type"));
        doc.setBrand(row.getString("brand"));
        doc.setModel(row.getString("model"));
        doc.setFuelType(row.getInteger("fuel_type"));
        doc.setOwnerId(row.getUUID("owner_id"));
        doc.setStatus(row.getInteger("status"));
        JsonObject localizedNameJson = row.getJsonObject(COLUMN_LOCALIZED_NAME);
        if (localizedNameJson != null) {
            EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
            localizedNameJson.getMap().forEach((key, value) -> localizedName.put(LanguageCode.valueOf(key), (String) value));
            doc.setLocalizedName(localizedName);
        }
        return doc;
    }
}
