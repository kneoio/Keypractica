package io.kneo.officeframe.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Label;
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
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.LABEL;

@ApplicationScoped
public class LabelRepository extends AsyncRepository {

    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(LABEL);
    private static final String BASE_REQUEST = String.format("SELECT * FROM %s", entityData.getTableName());

    @Inject
    public LabelRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }


    public Uni<List<Label>> getAll(final int limit, final int offset) {
        String sql = BASE_REQUEST;
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(entityData.getTableName());
    }

    public Uni<List<Label>> getOfCategory(String categoryName) {
        String sql = String.format("SELECT * FROM %s WHERE category=$1", entityData.getTableName());
        return client.preparedQuery(sql)
                .execute(Tuple.of(categoryName))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Label> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    public Uni<List<Label>> findForDocument(UUID uuid, String labelTable ) {
        String sql = String.format("SELECT rl.* FROM %s ptl, %s rl where ptl.id = $1 and ptl.label_id = rl.id",
                labelTable, entityData.getTableName());
        return client.preparedQuery(sql)
                .execute(Tuple.of(uuid))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Label> findByIdentifier(String identifier) {
        return client.preparedQuery(BASE_REQUEST + " WHERE identifier = $1")
                .execute(Tuple.of(identifier))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        LOGGER.warn(String.format("No %s found with identifier: " + identifier, entityData.getTableName()));
                        return null;
                    }
                });
    }

    private Label from(Row row) {
        Label doc = new Label();
        setDefaultFields(doc, row);
        doc.setIdentifier(row.getString("identifier"));
        doc.setColor(row.getString("color"));
        doc.setCategory(row.getString("category"));
        doc.setHidden(row.getBoolean("hidden"));
        doc.setParent(row.getUUID("parent"));
        setLocalizedNames(doc, row);
        return doc;
    }

    public Label update(Label node) {
        return node;
    }

    public int delete(Long id) {
        return 1;
    }


}
