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
    private static final String TABLE_NAME = "ref__labels";
    private static final String ENTITY_NAME = "label";
    private static final String BASE_REQUEST = String.format("SELECT * FROM %s", TABLE_NAME);
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(LABEL);

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
        return getAllCount(TABLE_NAME);
    }

    public Uni<Optional<Label>> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    public Uni<Optional<Label>> findByIdentifier(String identifier) {
        return client.preparedQuery(BASE_REQUEST + " WHERE identifier = $1")
                .execute(Tuple.of(identifier))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return Optional.of(from(iterator.next()));
                    } else {
                        LOGGER.warn(String.format("No %s found with identifier: " + identifier, ENTITY_NAME));
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

  /*  public Uni<UUID> insert(Task doc, Long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("INSERT INTO %s" +
                "(reg_date, author, last_mod_date, last_mod_user, assignee, body, target_date, priority, start_date, status, title, parent_id, project_id, task_type_id, reg_number, status_date, cancel_comment)" +
                "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17);", TABLE_NAME);
        Tuple params = Tuple.of(nowTime, user, nowTime, user);
        Tuple allParams = params
                .addLong(doc.getAssignee())
                .addString(doc.getBody())
                .addLocalDateTime(doc.getTargetDate().toLocalDateTime())
                .addInteger(doc.getPriority())
                .addLocalDateTime(doc.getStartDate().toLocalDateTime())
                .addInteger(doc.getStatus())
                .addString(doc.getTitle())
                .addUUID(doc.getParent())
                .addUUID(doc.getProject())
                .addUUID(doc.getTaskType())
                .addString(doc.getRegNumber())
                .addLocalDateTime(doc.getStartDate().toLocalDateTime())
                .addString(doc.getCancellationComment());
        String readersSql = String.format("INSERT INTO %s(reader, entity_id, can_edit, can_delete) VALUES($1, $2, $3, $4, $5)", ACCESS_TABLE_NAME);
        return client.withTransaction(tx -> {
            return tx.preparedQuery(sql)
                    .execute(allParams)
                    .onItem().transform(result -> result.iterator().next().getUUID("id"))
                    .onFailure().recoverWithUni(throwable -> {
                        LOGGER.error(throwable.getMessage());
                        return Uni.createFrom().failure(new RuntimeException(String.format("Failed to insert to %s", ENTITY_NAME), throwable));
                    });
        });
    }*/


    public Label update(Label node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }
}
