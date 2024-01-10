package io.kneo.officeframe.repository;

import io.kneo.core.model.user.SuperUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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

    private static final EntityData EMPLOYEE_ENTITY_DATA = OfficeFrameNameResolver.create().getEntityNames(EMPLOYEE);
    @Inject
    PgPool client;

    public Uni<List<Employee>> getAll(final int limit, final int offset) {
        String sql = String.format("SELECT * FROM %s ORDER BY rank", EMPLOYEE_ENTITY_DATA.tableName());
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(EMPLOYEE_ENTITY_DATA.tableName());
    }

    public Uni<List<Employee>> search(String keyword) {
        String query = String.format(
                "(SELECT 0 as id, id as uuid, name, phone, NULL as email FROM %s WHERE textsearch @@ to_tsquery('english', '%s')) " +
                        "UNION " +
                        "(SELECT id, NULL, login as name, 'phone', email FROM %s WHERE textsearch @@ to_tsquery('english', '%s'))",
                EMPLOYEE_ENTITY_DATA.tableName(), keyword, "_users", keyword
        );
        return client.query(query)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transformToUniAndMerge(this::fromAny)
                .collect().asList();
    }

    public Uni<Optional<Employee>> findById(UUID uuid) {
        return client.preparedQuery("SELECT * FROM staff__employees se WHERE se.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<Optional<Employee>> findByUserId(long id) {
        return client.preparedQuery("SELECT * FROM staff__employees se WHERE se.user_id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Optional<Organization> findByValue(String base) {
        return Optional.empty();
    }

    private Employee from(Row row) {
        Employee employee = new Employee();
        employee.setId(row.getUUID("id"));
        employee.setUser(row.getLong("user_id"));
        employee.setOrganization(row.getUUID("organization_id"));
        employee.setDepartment(row.getUUID("department_id"));
        employee.setPosition(row.getUUID("position_id"));
        employee.setName(row.getString("name"));
        employee.setPhone(row.getString("phone"));
        employee.setBirthDate(row.getLocalDate("birth_date"));
        employee.setStatus(row.getInteger("status"));
        employee.setFired(row.getBoolean("fired"));
        return employee;
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
            return Uni.createFrom().failure(new IllegalArgumentException("Unsupported type for id: " + EMPLOYEE_ENTITY_DATA));
        }
    }

    public Uni<UUID> insert(Employee doc, long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("INSERT INTO %s " +
                "(reg_date, author, last_mod_date, last_mod_user, status, birth_date, name, department_id, organization_id, position_id, user_id, fired, rank, loc_name, phone) " +
                "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15) RETURNING id", EMPLOYEE_ENTITY_DATA.tableName());
        Tuple params = Tuple.of(nowTime, user, nowTime, user);
        Tuple allParams = params
                .addInteger(doc.getStatus())
                .addLocalDate(doc.getBirthDate())
                .addString(doc.getName());

        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(allParams)
                .onItem().transform(result -> result.iterator().next().getUUID("id"))
                .onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage());
                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to insert to %s", EMPLOYEE), throwable));
                }));
    }


    public Organization update(Organization node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }

}
