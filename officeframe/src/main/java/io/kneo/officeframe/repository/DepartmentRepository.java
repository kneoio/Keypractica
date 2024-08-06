package io.kneo.officeframe.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Department;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.DEPARTMENT;

@ApplicationScoped
public class DepartmentRepository extends AsyncRepository {

    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(DEPARTMENT);

    @Inject
    public DepartmentRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }


    public Uni<List<Department>> getAll(final int limit, final int offset) {
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

    public Uni<List<Department>> getOfOrg(UUID orgId) {
        String sql = String.format("SELECT * FROM %s WHERE organization_id=$1 ORDER BY rank", entityData.getTableName());
        return client.preparedQuery(sql)
                .execute(Tuple.of(orgId))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Department> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    private Department from(Row row) {
        Department doc = new Department();
        setDefaultFields(doc, row);
        doc.setIdentifier(row.getString("identifier"));
        doc.setType(row.getUUID("type_id"));
        doc.setOrganization(row.getUUID("organization_id"));
        doc.setLeadDepartment(row.getUUID("lead_department_id"));
        doc.setRank(row.getInteger("rank"));
        setLocalizedNames(doc, row);
        return doc;
    }

    public Uni<Department> insert(Department doc, IUser user) {
        return null;
    }

    public Uni<Department> update(UUID uuid, Department doc, IUser user) {
        return null;
    }

    public Uni<Integer> delete(UUID id) {
        return client.preparedQuery(String.format("DELETE FROM %s WHERE id=$1", entityData.getTableName()))
                .execute(Tuple.of(id))
                .onItem().transformToUni(rowSet -> {
                    int rowCount = rowSet.rowCount();
                    if (rowCount == 0) {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                    }
                    return Uni.createFrom().item(rowCount);
                });
    }

    public Uni<Department> findByIdentifier(String identifier) {
        return null;
    }

}
