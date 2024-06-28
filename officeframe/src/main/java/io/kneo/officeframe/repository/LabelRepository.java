package io.kneo.officeframe.repository;

import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Label;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.LABEL;

@ApplicationScoped
public class LabelRepository extends AsyncRepository {

    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(LABEL);
    private static final String BASE_REQUEST = String.format("SELECT * FROM %s", entityData.getTableName());



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

    public Uni<Optional<Label>> findById(UUID uuid) {
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

    public Uni<Optional<Label>> findByIdentifier(String identifier) {
        return client.preparedQuery(BASE_REQUEST + " WHERE identifier = $1")
                .execute(Tuple.of(identifier))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return Optional.of(from(iterator.next()));
                    } else {
                        LOGGER.warn(String.format("No %s found with identifier: " + identifier, entityData.getTableName()));
                        return Optional.empty();
                    }
                });
    }

    private Label from(Row row) {
        Label label = new Label();
        label.setId(row.getUUID("id"));
        label.setAuthor(row.getLong("author"));
        label.setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()));
        label.setLastModifier(row.getLong("last_mod_user"));
        label.setRegDate(row.getLocalDateTime("last_mod_date").atZone(ZoneId.systemDefault()));
        label.setIdentifier(row.getString("identifier"));
        label.setColor(row.getString("color"));
        label.setCategory(row.getString("category"));
        label.setHidden(row.getBoolean("hidden"));
        label.setParent(row.getUUID("parent"));
        return label;
    }

    public Label update(Label node) {
        return node;
    }

    public int delete(Long id) {
        return 1;
    }
}
