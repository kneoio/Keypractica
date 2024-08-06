package io.kneo.officeframe.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.ORG_CATEGORY;

@ApplicationScoped
public class OrgCategoryRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(ORG_CATEGORY);

    @Inject
    public OrgCategoryRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }

    public Uni<List<OrgCategory>> getAll(final int limit, final int offset) {
        String sql = String.format("SELECT * FROM %s ", entityData.getTableName());
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

    public Uni<OrgCategory> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    private OrgCategory from(Row row) {
        OrgCategory doc = new OrgCategory();
        setDefaultFields(doc, row);
        doc.setIdentifier(row.getString(COLUMN_IDENTIFIER));
        setLocalizedNames(doc, row);
        return doc;
    }

}
