package io.kneo.officeframe.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.model.TaskType;
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
import java.util.Optional;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.TASK_TYPE;

@ApplicationScoped
public class TaskTypeRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(TASK_TYPE);
    private static final String BASE_REQUEST = String.format("SELECT * FROM %s t ", entityData.getTableName());


    @Inject
    public TaskTypeRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }


    public Uni<List<TaskType>> getAll(final int limit, final int offset) {
        return client.query(getBaseSelect(BASE_REQUEST, limit, offset))
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(entityData.getTableName());
    }

    public Uni<TaskType> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    public Uni<TaskType> findByIdentifier(String identifier) {
        return findByIdentifier(identifier, entityData, this::from);
    }

    public Uni<Optional<TaskType>> findByUserId(long id) {
        return client.preparedQuery(BASE_REQUEST + " WHERE rtt.user_id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private TaskType from(Row row) {
        TaskType doc = new TaskType();
        setDefaultFields(doc, row);
        doc.setIdentifier(row.getString(COLUMN_IDENTIFIER));
        setLocalizedNames(doc, row);
        return doc;
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
