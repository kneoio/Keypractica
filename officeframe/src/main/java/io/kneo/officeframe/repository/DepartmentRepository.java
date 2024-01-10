package io.kneo.officeframe.repository;

import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.dto.DepartmentDTO;
import io.kneo.officeframe.model.Department;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.DEPARTMENT;

@ApplicationScoped
public class DepartmentRepository extends AsyncRepository {

    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(DEPARTMENT);
    @Inject
    PgPool client;

    public Uni<List<DepartmentDTO>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM staff__departments ORDER BY rank";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new DepartmentDTO(row.getString("name"))).collect().asList();
    }

    public Uni<Optional<Department>> findById(UUID uuid) {
        return findById(uuid, entityData, DepartmentRepository::from);
    }

    private static Department from(Row row) {
        Department doc = new Department();
        doc.setId(row.getUUID("id"));
        return doc;
    }

    public Optional<Organization> findByValue(String base) {
        return null;
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
