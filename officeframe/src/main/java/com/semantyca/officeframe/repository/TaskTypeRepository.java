package com.semantyca.officeframe.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.officeframe.dto.EmployeeDTO;
import com.semantyca.officeframe.model.Organization;
import com.semantyca.officeframe.model.TaskType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TaskTypeRepository {

    @Inject
    PgPool client;

    @Inject
    ObjectMapper mapper;

    private static final Logger LOGGER = LoggerFactory.getLogger("TaskTypeRepository");

    public Uni<List<EmployeeDTO>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM ref__task_types ORDER BY rank";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new EmployeeDTO(row.getUUID("id"), row.getString("name"))).collect().asList();
    }

    public Uni<Optional<TaskType>> findById(UUID uuid) {
        return client.preparedQuery("SELECT * FROM ref__task_types rtt WHERE rtt.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<Optional<TaskType>> findByUserId(long id) {
        return client.preparedQuery("SELECT * FROM ref__task_types rtt WHERE rtt.user_id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Optional<Organization> findByValue(String base) {
        return null;
    }

    private TaskType from(Row row) {
        Map<LanguageCode, String> map;
        try {
            map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return new TaskType.Builder()
                .setId(row.getUUID("id"))
                .setIdentifier(row.getString("identifier"))
                .setLocName(map)
                .build();
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
