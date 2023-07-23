package com.semantyca.projects.repository;

import com.semantyca.core.model.Language;
import com.semantyca.core.repository.AsyncRepository;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.model.Task;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskRepository extends AsyncRepository {

    private static final String BASE_REQUEST = """
            SELECT pt.*, ptr.*  FROM prj__tasks pt  JOIN prj__task_readers ptr ON pt.id = ptr.entity_id\s""";

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

    public Uni<Optional<Task>> findById(Long userID, UUID uuid) {
        return client.preparedQuery(BASE_REQUEST + "WHERE ptr.reader = $1 AND pt.id = $2")
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
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
                .setTaskType(row.getUUID("tasktype_id"))
                .setTargetDate(Optional.ofNullable(row.getLocalDateTime("target_date"))
                        .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault()))).orElse(null))
                .setStartDate(Optional.ofNullable(row.getLocalDateTime("start_date"))
                    .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault()))).orElse(null))
                .setStatus(row.getInteger("status"))
                .setPriority(row.getInteger("priority"))
                .setCancellationComment(row.getString("cancel_comment"))
                .setInitiative(Optional.ofNullable(row.getBoolean("initiative")).orElse(false))
                //.setTags()
                .build();
    }



    public UUID insert(Project node, Long user) {

        return node.getId();
    }


    public Language update(Language node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }
}
