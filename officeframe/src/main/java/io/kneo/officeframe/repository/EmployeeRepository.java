package io.kneo.officeframe.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.EMPLOYEE;

@ApplicationScoped
public class EmployeeRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(EMPLOYEE);

    @Inject
    public EmployeeRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }


    public Uni<List<Employee>> getAll(final int limit, final int offset) {
        String sql = String.format("SELECT * FROM %s ORDER BY rank", entityData.getTableName());
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(entityData.getTableName());
    }

    public Uni<List<Employee>> search(String keyword) {
        String query = String.format(
                "(SELECT 0 as id, id as uuid, name, phone, NULL as email FROM %s WHERE textsearch @@ to_tsquery('english', '%s')) " +
                        "UNION " +
                        "(SELECT id, NULL, login as name, 'phone', email FROM %s WHERE textsearch @@ to_tsquery('english', '%s'))",
                entityData.getTableName(), keyword, "_users", keyword
        );
        return client.query(query)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transformToUniAndMerge(this::fromAny)
                .collect().asList();
    }

    public Uni<Employee> getById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    public Uni<Employee> getByUserId(long id) {
        return client.preparedQuery(String.format("SELECT * FROM %s se WHERE se.user_id = $1", entityData.getTableName()))
                .execute(Tuple.of(id))
                .onItem().transformToUni(rowSet -> {
                    var iterator = rowSet.iterator();
                    if (iterator.hasNext()) {
                        return Uni.createFrom().item(from(iterator.next()));
                    } else {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException("Employee: "  + id));
                    }
                });
    }


    public Uni<Employee> getByIdentifier(String identifier) {
        return null;
    }

    private Employee from(Row row) {
        Employee doc = new Employee();
        setDefaultFields(doc, row);
        doc.setUserId(row.getLong("user_id"));
        doc.setOrganization(row.getUUID("organization_id"));
        doc.setDepartment(row.getUUID("department_id"));
        doc.setPosition(row.getUUID("position_id"));
    //    doc.setIdentifier(row.getString("identifier"));
        doc.setPhone(row.getString("phone"));
        doc.setBirthDate(row.getLocalDate("birth_date"));
        doc.setStatus(row.getInteger("status"));
        setLocalizedNames(doc, row);
        return doc;
    }

    private Uni<Employee> fromAny(Row row) {
        Object value = row.getValue("id");
        if (value instanceof UUID) {
            return Uni.createFrom().item(from(row));
        } else if (value instanceof Integer || value instanceof Long) {
            Employee employee = new Employee();
            employee.setPhone(row.getString("phone"));
            employee.setStatus(1);
            return insert(employee, SuperUser.build());
        } else {
            return Uni.createFrom().failure(new IllegalArgumentException("Unsupported type for id: " + entityData));
        }
    }

    public Uni<Employee> insert(Employee doc, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("INSERT INTO %s " +
                "(reg_date, author, last_mod_date, last_mod_user, status, birth_date, name, " +
                "department_id, organization_id, position_id, user_id, rank, loc_name, phone) " +
                "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14) RETURNING id", entityData.getTableName());
        Tuple params = Tuple.of(nowTime, user.getId(), nowTime, user.getId());
        Tuple allParams = params
                .addInteger(doc.getStatus())
                .addLocalDate(doc.getBirthDate())
                .addUUID(doc.getDepartment())
                .addUUID(doc.getOrganization())
                .addUUID(doc.getPosition())
                .addLong(doc.getUserId())
                .addInteger(doc.getRank())
                .addJsonObject(getLocalizedName(doc.getLocalizedName()))
                .addString(doc.getPhone());

        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(allParams)
                .onItem().transformToUni(result -> {
                    UUID id = result.iterator().next().getUUID("id");
                    return getById(id);
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to insert to %s", EMPLOYEE), throwable));
                }));
    }

    public Uni<Employee> update(UUID id, Employee doc, IUser user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("UPDATE %s SET reg_date=$1, author=$2, last_mod_date=$3, last_mod_user=$4, " +
                "status=$5, birth_date=$6, department_id=$7, organization_id=$8, position_id=$9, " +
                "user_id=$10, rank=$11, loc_name=$12, phone=$13 WHERE id=$14", entityData.getTableName());
        Tuple params = Tuple.of(nowTime, user.getId(), nowTime, user.getId())
                .addInteger(doc.getStatus())
                .addLocalDate(doc.getBirthDate())
                .addUUID(doc.getDepartment())
                .addUUID(doc.getOrganization())
                .addUUID(doc.getPosition())
                .addLong(doc.getUserId())
                .addInteger(doc.getRank())
                .addJsonObject(getLocalizedName(doc.getLocalizedName()))
                .addString(doc.getPhone());
        params.addUUID(id);
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(params)
                .onItem().transformToUni(result -> {
                    if (result.rowCount() > 0) {
                        return getById(id);
                    } else {
                        return Uni.createFrom().nullItem();
                    }
                })
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to update %s", EMPLOYEE), throwable));
                }));
    }



    public Uni<Integer> patch(UUID id, Map<String, Object> changes, long user) {
        if (changes.isEmpty()) {
            return Uni.createFrom().item(0);
        }

        List<Object> params = new ArrayList<>();
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        params.add(nowTime);
        params.add(user);

        String setClauses = changes.entrySet().stream()
                .map(entry -> {
                    params.add(entry.getValue());
                    return entry.getKey() + " = ?";
                })
                .collect(Collectors.joining(", "));

        String sql = String.format("UPDATE %s SET last_mod_date = ?, last_mod_user = ?, %s WHERE id = ?",
                entityData.getTableName(), setClauses);

        params.add(id);

        Tuple allParams = Tuple.from(params.toArray());

        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(allParams)
                .onItem().transform(result -> result.rowCount() > 0 ? 1 : 0));  // removed .onFailure().recoverWithUni()
    }


    public Uni<Integer> delete(UUID id) {
        String sql = String.format("DELETE FROM %s WHERE  id=$1", entityData.getTableName());
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(Tuple.of(id))
                .onItem().transform(result -> result.rowCount() > 0 ? 1 : 0)
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().item(0);
                }));
    }

    private JsonObject getLocalizedName(EnumMap<LanguageCode, String> localizedName) {
        try {
            return new JsonObject(mapper.writeValueAsString(localizedName));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
