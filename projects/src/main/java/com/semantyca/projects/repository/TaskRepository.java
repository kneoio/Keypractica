package com.semantyca.projects.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.core.repository.Repository;
import com.semantyca.officeframe.model.TaskType;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.model.Task;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskRepository extends Repository {
    @Inject
    ObjectMapper mapper;
    @Inject
    PgPool client;

    private static final String BASE_REQUEST = """
            SELECT pt.*, rt.*, rtt.id as task_type_id, rtt.identifier, rtt.loc_name as task_type_loc_name, rtt.author as task_type_author, rtt.reg_date as task_type_reg_date, rtt.last_mod_user as task_type_last_modifier, rtt.last_mod_date as task_type_last_mod_date 
            FROM prj__tasks pt
            JOIN prj__task_readers ptr ON pt.id = ptr.entity_id
            JOIN ref__task_types rtt ON rtt.id = pt.tasktype_id
            JOIN prj__task_tags tt ON pt.id = tt.task_id
            JOIN ref__tags rt ON tt.tags_id = rt.id\s""";

    public Uni<List<Task>> getAll(final int limit, final int offset, final long userID) {
        String sql = BASE_REQUEST + "WHERE ptr.reader = " + userID;
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> from(row))
                                .collect().asList();
    }

    public Uni<Optional<Task>> findById(Long userID, UUID uuid) {
        return client.preparedQuery(BASE_REQUEST + "WHERE ptr.reader = $1 AND pt.id = $2")
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<List<RLS>> getAllReaders(UUID uuid) {
        return client.preparedQuery("SELECT reader, reading_time, can_edit, can_delete FROM prj__tasks p, prj__task_readers ppr WHERE p.id = ppr.entity_id AND p.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new RLS(
                        Optional.ofNullable(row.getLocalDateTime("reading_time"))
                                .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault())))
                                .orElse(null),
                        row.getLong("reader"),
                        row.getLong("can_edit"),
                        row.getLong("can_delete")))
                .collect().asList();
    }

    private Task from(Row row) {
        Map<LanguageCode, String> taskTypeLocNameMap;
        try {
            taskTypeLocNameMap = mapper.readValue(row.getJsonObject("task_type_loc_name").toString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return new Task.Builder()
                .setId(row.getUUID("id"))
                .setRegNumber(row.getString("reg_number"))
                .setAssignee(row.getLong("assignee"))
                .setBody(row.getString("body"))
                .setTaskType(new TaskType.Builder()
                        .setId(row.getUUID("task_type_id"))
                        .setIdentifier(row.getString("identifier"))
                        .setLocName(taskTypeLocNameMap)
                        .setAuthor(row.getLong("task_type_author"))
                        .setRegDate(Optional.ofNullable(row.getLocalDateTime("task_type_reg_date"))
                                .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault()))).orElse(null))
                        .setLastModifier(row.getLong("task_type_last_modifier"))
                        .setLastModifiedDate(Optional.ofNullable(row.getLocalDateTime("task_type_last_mod_date"))
                                .map(dateTime -> ZonedDateTime.from(dateTime.atZone(ZoneId.systemDefault()))).orElse(null))
                        .build())
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
