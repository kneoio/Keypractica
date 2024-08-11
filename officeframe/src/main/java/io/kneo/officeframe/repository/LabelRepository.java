package io.kneo.officeframe.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Label;
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

    public Uni<List<Label>> findForDocument(UUID uuid, String labelTable) {
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

    public Uni<Label> insert(Label doc, IUser user) {
        String sql = String.format("INSERT INTO %s (id, author, reg_date, last_mod_user, last_mod_date, identifier, color, category, parent, hidden, loc_name) " +
                        "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11) RETURNING id",
                entityData.getTableName());

        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
        LocalDateTime now = LocalDateTime.now();
        UUID id = UUID.randomUUID();

        Tuple params = Tuple.of(id)
                .addLong(user.getId())
                .addLocalDateTime(now)
                .addLong(user.getId())
                .addLocalDateTime(now)
                .addString(doc.getIdentifier())
                .addString(doc.getColor())
                .addString(doc.getCategory())
                .addUUID(doc.getParent())
                .addBoolean(doc.isHidden())
                .addJsonObject(localizedNameJson);

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transformToUni(result -> {
                    UUID generatedId = result.iterator().next().getUUID("id");
                    return findById(generatedId);
                });
    }

    public Uni<Label> update(UUID id, Label doc, IUser user) {
        String sql = String.format("UPDATE %s SET %s=$1, %s=$2, %s=$3, %s=$4, %s=$5, %s=$6, %s=$7, %s=$8 WHERE id=$9",
                entityData.getTableName(),
                COLUMN_LAST_MOD_USER,
                COLUMN_LAST_MOD_DATE,
                COLUMN_IDENTIFIER,
                "color",
                "category",
                "parent",
                "hidden",
                COLUMN_LOCALIZED_NAME);

        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
        LocalDateTime now = LocalDateTime.now();

        Tuple params = Tuple.of(user.getId())
                .addLocalDateTime(now)
                .addString(doc.getIdentifier())
                .addString(doc.getColor())
                .addString(doc.getCategory())
                .addUUID(doc.getParent())
                .addBoolean(doc.isHidden())
                .addJsonObject(localizedNameJson)
                .addUUID(id);

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transformToUni(rowSet -> {
                    if (rowSet.rowCount() == 0) {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                    }
                    return findById(id);
                });
    }

    public int delete(Long id) {
        return 1;
    }
}
