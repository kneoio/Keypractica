package io.kneo.officeframe.repository;

import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.dto.EmployeeDTO;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.EMPLOYEE;

@ApplicationScoped
public class EmployeeRepository extends AsyncRepository {

    private static final EntityData EMPLOYEE_ENTITY_DATA = OfficeFrameNameResolver.create().getEntityNames(EMPLOYEE);
    @Inject
    PgPool client;

    public Uni<List<EmployeeDTO>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM staff__employees ORDER BY rank";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new EmployeeDTO(row.getUUID("id"), row.getString("name"))).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(EMPLOYEE_ENTITY_DATA.mainName());
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
        return new Employee.Builder()
                .setId(row.getUUID("id"))
                .setName(row.getString("name"))
                .setPhone(row.getString("phone"))
                .build();
    }

    public UUID insert(Organization node, Long user) {

        return node.getId();
    }


    public Organization update(Organization node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }


}
