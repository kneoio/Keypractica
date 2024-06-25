package io.kneo.officeframe.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.model.TaskType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskTypeRepository extends AsyncRepository {
    private static final String TABLE_NAME = "ref__task_types";
    private static final String ENTITY_NAME = "task_type";
    private static final String BASE_REQUEST = String.format("SELECT * FROM %s t ", TABLE_NAME);
    private static final Logger LOGGER = LoggerFactory.getLogger("TaskTypeRepository");

    protected TaskTypeRepository() {
        super(null, null);
    }

    public TaskTypeRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper);
    }

    public Uni<List<TaskType>> getAll(final int limit, final int offset) {
        return client.query(getBaseSelect(BASE_REQUEST, limit, offset))
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(TABLE_NAME);
    }

    public Uni<Optional<TaskType>> findById(UUID uuid) {
        return client.preparedQuery(BASE_REQUEST + " WHERE t.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<Optional<TaskType>> findByIdentifier(String  identifier) {
        return client.preparedQuery(BASE_REQUEST + " WHERE t.identifier = $1")
                .execute(Tuple.of(identifier))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<Optional<TaskType>> findByUserId(long id) {
        return client.preparedQuery(BASE_REQUEST + " WHERE rtt.user_id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private TaskType from(Row row) {
        EnumMap<LanguageCode, String> map;
        try {
            map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        TaskType taskType = new TaskType();
        taskType.setId(row.getUUID("id"));
        taskType.setAuthor(row.getLong("author"));
        taskType.setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()));
        taskType.setLastModifier(row.getLong("last_mod_user"));
        taskType.setRegDate(row.getLocalDateTime("last_mod_date").atZone(ZoneId.systemDefault()));
        taskType.setIdentifier(row.getString("identifier"));
        taskType.setLocalizedName(map);
        return taskType;
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
