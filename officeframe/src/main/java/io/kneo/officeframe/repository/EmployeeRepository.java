package io.kneo.officeframe.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
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
import java.util.*;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.EMPLOYEE;

@ApplicationScoped
public class EmployeeRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(EMPLOYEE);
    @Inject
    PgPool client;

    @Inject
    ObjectMapper mapper;

    public Uni<List<Employee>> getAll(final int limit, final int offset) {
        String sql = String.format("SELECT * FROM %s ORDER BY rank", entityData.tableName());
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(entityData.tableName());
    }

    public Uni<List<Employee>> search(String keyword) {
        String query = String.format(
                "(SELECT 0 as id, id as uuid, name, phone, NULL as email FROM %s WHERE textsearch @@ to_tsquery('english', '%s')) " +
                        "UNION " +
                        "(SELECT id, NULL, login as name, 'phone', email FROM %s WHERE textsearch @@ to_tsquery('english', '%s'))",
                entityData.tableName(), keyword, "_users", keyword
        );
        return client.query(query)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transformToUniAndMerge(this::fromAny)
                .collect().asList();
    }

    public Uni<Optional<Employee>> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    public Uni<Optional<Employee>> findByUserId(long id) {
        return client.preparedQuery("SELECT * FROM staff__employees se WHERE se.user_id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private Employee from(Row row) {
        Employee doc = new Employee();
        setDefaultFields(doc, row);
        doc.setUser(row.getLong("user_id"));
        doc.setOrganization(row.getUUID("organization_id"));
        doc.setDepartment(row.getUUID("department_id"));
        doc.setPosition(row.getUUID("position_id"));
        doc.setName(row.getString("name"));
        doc.setLocalizedName(getLocalizedName(row));
        doc.setPhone(row.getString("phone"));
        doc.setBirthDate(row.getLocalDate("birth_date"));
        doc.setStatus(row.getInteger("status"));
        doc.setFired(row.getBoolean("fired"));
        return doc;
    }

    private Uni<Employee> fromAny(Row row) {
        Object value = row.getValue("id");
        if (value instanceof UUID) {
            return Uni.createFrom().item(from(row));
        } else if (value instanceof Integer || value instanceof Long) {
            Employee employee = new Employee();
            employee.setName(row.getString("name"));
            employee.setPhone(row.getString("phone"));
            employee.setStatus(1);
            return insert(employee, SuperUser.ID)
                    .onItem().transform(uuid -> {
                        employee.setId(uuid);
                        return employee;
                    });
        } else {
            return Uni.createFrom().failure(new IllegalArgumentException("Unsupported type for id: " + entityData));
        }
    }

    public Uni<UUID> insert(Employee doc, long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("INSERT INTO %s " +
                "(reg_date, author, last_mod_date, last_mod_user, status, birth_date, name, " +
                "department_id, organization_id, position_id, user_id, fired, rank, loc_name, phone) " +
                "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15) RETURNING id", entityData.tableName());
        Tuple params = Tuple.of(nowTime, user, nowTime, user);
        Tuple allParams = params
                .addInteger(doc.getStatus())
                .addLocalDate(doc.getBirthDate())
                .addString(doc.getName())
                .addUUID(doc.getDepartment())
                .addUUID(doc.getOrganization())
                .addUUID(doc.getPosition())
                .addLong(doc.getUser())
                .addBoolean(doc.isFired())
                .addInteger(doc.getRank())
                .addJsonObject(getLocalizedName(doc.getLocalizedName()))
                .addString(doc.getPhone());

        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(allParams)
                .onItem().transform(result -> result.iterator().next().getUUID("id"))
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to insert to %s", EMPLOYEE), throwable));
                }));
    }

    private JsonObject getLocalizedName(EnumMap<LanguageCode, String> localizedName) {
        try {
            return new JsonObject(mapper.writeValueAsString(localizedName));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private EnumMap<LanguageCode, String> getLocalizedName(Row row) {
        try {
            JsonObject localizedNameJson = row.getJsonObject("loc_name");
            Map i = localizedNameJson.getMap();
            return  convertToEnumMap(i);
        } catch (Exception e) {
            return new EnumMap<>(LanguageCode.class);
        }
    }

    private EnumMap<LanguageCode, String> convertToEnumMap(Map<String, String> linkedHashMap) {
        EnumMap<LanguageCode, String> enumMap = new EnumMap<>(LanguageCode.class);

        for (Map.Entry<String, String> entry : linkedHashMap.entrySet()) {
            try {
                LanguageCode key = LanguageCode.valueOf(entry.getKey().toUpperCase());
                enumMap.put(key, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Handle the case where the key is not a valid enum value
                // This depends on how you want to handle invalid keys
            }
        }

        return enumMap;
    }


    public Uni<Integer> update(Organization node) {

        return (Uni<Integer>) node;
    }

    public int delete(Long id) {

        return 1;
    }

}
