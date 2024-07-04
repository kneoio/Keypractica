package io.kneo.projects.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.vertx.mutiny.pgclient.PgPool;
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

    @Inject
    public ProjectRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper);
    }


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
        return client.preparedQuery(String.format("SELECT theTable.*, rls.* FROM %s theTable JOIN %s rls ON theTable.id = rls.entity_id " +
                        "WHERE rls.reader = $1 AND theTable.id = $2", entityData.getTableName(), entityData.getRlsName()))
                .execute(Tuple.of(userID, uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return Optional.of(from(iterator.next()));
                    } else {
                        LOGGER.warn(String.format("No %s found with id: " + uuid, entityData.getTableName()));
                        return Optional.empty();
                    }
                });
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
        Project doc = new Project();
        setDefaultFields(doc, row);
        doc.setName(row.getString("name"));
        doc.setStatus(ProjectStatusType.valueOf(row.getString("status")));
        doc.setFinishDate(row.getLocalDate("finish_date"));
        //TODO reg_date is temporary, start_date need to add to the table
        doc.setStartDate(row.getLocalDate("reg_date"));
        doc.setPrimaryLang(LanguageCode.getType(row.getInteger("primary_lang")));
        doc.setManager(row.getLong("manager"));
        doc.setCoder(row.getLong("programmer"));
        doc.setTester(row.getLong("tester"));
       return doc;
    }


    public Uni<Project> insert(Project doc, Long user) {
        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
        String sql = String.format("INSERT INTO %s" +
                "(reg_date, author, last_mod_date, last_mod_user, name, status, finish_date, primary_lang, manager, programmer, tester)" +
                "VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11) RETURNING id;", entityData.getTableName());

        Tuple params = Tuple.of(nowTime, user, nowTime, user);
        Tuple allParams = params
                .addString(doc.getName())
                .addString(doc.getStatus().toString())
                .addLocalDateTime(doc.getFinishDate().atStartOfDay())
                .addInteger(doc.getPrimaryLang().getCode())
                .addLong(doc.getManager())
                .addLong(doc.getCoder())
                .addLong(doc.getTester());

        String readersSql = String.format("INSERT INTO %s(reader, entity_id, can_edit, can_delete) VALUES($1, $2, $3, $4)", entityData.getRlsName());

        return client.withTransaction(tx -> {
            return tx.preparedQuery(sql)
                    .execute(allParams)
                    .onItem().transform(result -> result.iterator().next().getUUID("id"))
                    .onFailure().recoverWithUni(t ->
                            Uni.createFrom().failure(t))
                    .onItem().transformToUni(id -> {
                        return tx.preparedQuery(readersSql)
                                .execute(Tuple.of(user, id, 1, 1))
                                .onItem().ignore().andContinueWithNull()
                                .onFailure().recoverWithUni(t ->
                                        Uni.createFrom().failure(t))
                                .onItem().transform(unused -> id);
                    });
        }).onItem().transformToUni(id -> findById(id, user)
                .onItem().transform(optionalProject -> optionalProject.orElseThrow(() -> new RuntimeException("Failed to retrieve inserted project"))));
    }

    public Uni<Project> update(UUID id, Project doc, Long user) {
        return rlsRepository.findById(entityData.getRlsName(), user, id)
                .onItem().transformToUni(permissions -> {
                    if (permissions[0] == 1) {
                        LocalDateTime nowTime = ZonedDateTime.now().toLocalDateTime();
                        String sql = String.format("UPDATE %s " +
                                "SET last_mod_date=$1, last_mod_user=$2, name=$3, status=$4, finish_date=$5, " +
                                "primary_lang=$6, manager=$7, programmer=$8, " +
                                "tester=$9 WHERE id=$10;", entityData.getTableName());
                        Tuple baseParams = Tuple.of(nowTime, user);
                        Tuple allParams = baseParams
                                .addString(doc.getName())
                                .addString(doc.getStatus().toString())
                                .addLocalDateTime(doc.getFinishDate().atStartOfDay())
                                .addInteger(doc.getPrimaryLang().getCode())
                                .addLong(doc.getManager())
                                .addLong(doc.getCoder())
                                .addLong(doc.getTester())
                                .addUUID(id);

                        return client.withTransaction(tx -> tx.preparedQuery(sql)
                                .execute(allParams)
                                .onItem().transformToUni(rowSet -> {
                                    int rowCount = rowSet.rowCount();
                                    if (rowCount == 0) {
                                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                                    }
                                    return findById(id, user)
                                            .onItem().transform(optionalProject -> optionalProject.orElseThrow(() -> new RuntimeException("Failed to retrieve updated project")));
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
