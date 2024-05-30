package io.kneo.projects.repository;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.embedded.RLS;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.rls.RLSRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.projects.model.Project;
import io.kneo.projects.model.cnst.ProjectStatusType;
import io.kneo.projects.repository.table.ProjectNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.projects.repository.table.ProjectNameResolver.PROJECT;

@ApplicationScoped
public class ProjectRepository extends AsyncRepository {
    private static final EntityData entityData = ProjectNameResolver.create().getEntityNames(PROJECT);
    @Inject
    private RLSRepository rlsRepository;

    public Uni<List<Project>> getAll(final int limit, final int offset, final long userID) {
        String sql = "SELECT * FROM prj__projects p, prj__project_readers ppr WHERE p.id = ppr.entity_id AND ppr.reader = " + userID;
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
        return getAllCount(userID, entityData.getTableName(), entityData.getRlsName());
    }

    public Uni<List<Project>> search(String keyword) {
        String query = String.format(
                "SELECT * FROM %s WHERE textsearch @@ to_tsquery('english', '%s')",
                entityData.getTableName(),
                keyword
        );
        return client.query(query)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<List<Project>> searchByCondition(String cond) {
        String query = String.format("SELECT * FROM %s WHERE %s ", entityData.getTableName(), cond);
        return client.query(query)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Optional<Project>> findById(UUID uuid, Long userID) {
        return client.preparedQuery("SELECT * FROM prj__projects p, prj__project_readers ppr WHERE p.id = ppr.entity_id  AND p.id = $1 AND ppr.reader = $2")
                .execute(Tuple.of(uuid, userID))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<List<RLS>> getAllReaders(UUID uuid) {
        return client.preparedQuery("SELECT reader, reading_time, can_edit, can_delete FROM prj__projects p, prj__project_readers ppr WHERE p.id = ppr.entity_id AND p.id = $1")
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

    private Project from(Row row) {
        return new Project.Builder()
                .setId(row.getUUID("id"))
                .setName(row.getString("name"))
                .setStatus(ProjectStatusType.valueOf(row.getString("status")))
                .setFinishDate(row.getLocalDate("finish_date"))
                .setPrimaryLang(LanguageCode.valueOf(row.getString("primary_language")))
                .setManager(row.getLong("manager"))
                .setCoder(row.getLong("programmer"))
                .setTester(row.getLong("tester"))
                .build();
    }


    public Uni<Optional<Project>> insert(Project node, Long user) {

        return null;
    }

    public Uni<Integer> update(UUID id, Project doc, Long user) {
        return rlsRepository.findById(entityData.getRlsName(), user, id)
                .onItem().transformToUni(permissions -> {
                    if (permissions[0] == 1) {  // Assuming index 0 is the edit permission
                        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
                        String sql = String.format("UPDATE %s SET name=$1, status=$2, finish_date=$3, primary_language=$4, manager=$5, programmer=$6, tester=$7, last_mod_date=$8, last_mod_user=$9 WHERE id=$10;", entityData.getTableName());

                        Tuple baseParams = Tuple.of(nowTime, user, nowTime, user);
                        Tuple allParams = baseParams
                                .addString(doc.getName())
                                .addString(doc.getStatus().toString())
                                .addLocalDate(doc.getFinishDate())
                                .addString(doc.getPrimaryLang().toString())
                                .addLong(doc.getManager())
                                .addLong(doc.getCoder())
                                .addLong(doc.getTester())
                                .addLocalDateTime(nowTime) // last_mod_date
                                .addLong(user) // last_mod_user
                                .addUUID(id);

                        return client.withTransaction(tx -> tx.preparedQuery(sql)
                                .execute(allParams)
                                .onItem().transformToUni(rowSet -> {
                                    int rowCount = rowSet.rowCount();
                                    if (rowCount == 0) {
                                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                                    }
                                    return Uni.createFrom().item(rowCount);
                                })
                                .onFailure().recoverWithUni(t -> Uni.createFrom().failure(t)));
                    } else {
                        return Uni.createFrom().failure(new DocumentModificationAccessException("User does not have edit permission", user, id));
                    }
                });
    }



    public Uni<Void> delete(UUID uuid) {
        return delete(uuid, entityData.getTableName());
    }


}
