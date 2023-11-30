package io.kneo.projects.repository;

import io.kneo.core.model.user.SuperUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.projects.model.Task;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskRepository extends AsyncRepository {

    private static final String TABLE_NAME = "prj__tasks";
    private static final String ACCESS_TABLE_NAME = "prj__task_readers";
    private static final String ENTITY_NAME = "task";

    private static final String BASE_REQUEST = """
            SELECT pt.*, ptr.*  FROM prj__tasks pt JOIN prj__task_readers ptr ON pt.id = ptr.entity_id\s""";

    public Uni<List<Task>> getAll(final int limit, final int offset, final long userID) {
        String sql = BASE_REQUEST + "WHERE ptr.reader = " + userID;
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Integer> getAllCount(long userID) {
        return getAllCount(userID, "prj__tasks", "prj__task_readers");
    }

    public Uni<Task> findById(Long userID, UUID uuid) {
        return client.preparedQuery(BASE_REQUEST + "WHERE ptr.reader = $1 AND pt.id = $2")
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        throw new NotFoundException("No item found for userID: " + userID + " and uuid: " + uuid);
                    }
                });
    }


    private Task from(Row row) {
        return new Task.Builder()
                .setId(row.getUUID("id"))
                .setAuthor(row.getLong("author"))
                .setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()))
                .setLastModifier(row.getLong("last_mod_user"))
                .setLastModifiedDate(row.getLocalDateTime("last_mod_date").atZone(ZoneId.systemDefault()))
                .setRegNumber(row.getString("reg_number"))
                .setAssignee(row.getLong("assignee"))
                .setBody(row.getString("body"))
                .setProject(row.getUUID("project_id"))
                .setParent(row.getUUID("parent_id"))
                .setTaskType(row.getUUID("task_type_id"))
                .setTargetDate(Optional.ofNullable(row.getLocalDateTime("target_date"))
                        .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault()))).orElse(null))
                .setStartDate(Optional.ofNullable(row.getLocalDateTime("start_date"))
                        .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault()))).orElse(null))
                .setStatus(row.getInteger("status"))
                .setPriority(row.getInteger("priority"))
                .setCancellationComment(row.getString("cancel_comment"))
                //.setTags()
                .build();
    }

    private Uni<RuntimeException> clarifyException(UUID uuid) {
        Uni<Task> taskUni = findById(SuperUser.build().getId(), uuid)
                .onItem().ifNotNull().failWith(new DocumentHasNotFoundException("Task found"))
                .onItem().ifNull().failWith(new DocumentHasNotFoundException("Task not found"));

        return taskUni.onItem().transform(task -> {
            if (task == null) {
                return new RuntimeException("Task not found");
            } else {
                return new RuntimeException("Task found");
            }
        });
    }

    public Uni<UUID> insert(Task doc, Long user) {
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
                    })
                    .onItem().transformToUni(id -> {
                        return tx.preparedQuery(readersSql)
                                .execute(Tuple.of(user, id, 1, 1))
                                .onItem().ignore().andContinueWithNull()
                                .onFailure().recoverWithUni(throwable -> {
                                    LOGGER.error(throwable.getMessage());
                                    return Uni.createFrom().failure(new RuntimeException(String.format("Failed to add %s", ACCESS_ENTITY_NAME), throwable));
                                })
                                .onItem().transform(unused -> id);
                    });
        });
    }


    public Task update(Task node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }
}
