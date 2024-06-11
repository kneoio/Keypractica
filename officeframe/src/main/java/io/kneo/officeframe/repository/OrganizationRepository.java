package io.kneo.officeframe.repository;

import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Organization;
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

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.ORGANIZATION;

@ApplicationScoped
public class OrganizationRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(ORGANIZATION);
    @Inject
    PgPool client;

    public Uni<List<Organization>> getAll(final int limit, final int offset) {
        String sql = String.format("SELECT * FROM %s ORDER BY rank", entityData.getTableName());
        if (limit > 0) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Integer> getAllCount() {
        return getAllCount(entityData.getTableName());
    }

    public Uni<Optional<Organization>> findById(UUID uuid) {
        return client.preparedQuery(String.format("SELECT * FROM %s WHERE id = $1", entityData.getTableName()))
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return Optional.of(from(iterator.next()));
                    } else {
                        return Optional.empty();
                    }
                });
    }

    public Uni<List<Organization>> search(String keyword) {
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

    public Uni<List<Organization>> searchByCondition(String cond) {
        String query = String.format("SELECT * FROM %s WHERE %s ", entityData.getTableName(), cond);
        return client.query(query)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<UUID> insert(Organization doc) {
        String sql = String.format("INSERT INTO %s (id, identifier, org_category_id, biz_id, rank) VALUES ($1, $2, $3, $4, $5) RETURNING id", entityData.getTableName());
        Tuple params = Tuple.of(doc.getId(), doc.getIdentifier(), doc.getOrgCategory(), doc.getBizID(), doc.getRank());

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transform(result -> result.iterator().next().getUUID("id"));
    }

    public Uni<Integer> update(UUID id, Organization doc) {
        String sql = String.format("UPDATE %s SET identifier=$1, org_category_id=$2, biz_id=$3, rank=$4 WHERE id=$5", entityData.getTableName());
        Tuple params = Tuple.of(doc.getIdentifier(), doc.getOrgCategory(), doc.getBizID(), doc.getRank(), id);

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transformToUni(rowSet -> {
                    int rowCount = rowSet.rowCount();
                    if (rowCount == 0) {
                        return Uni.createFrom().failure(new Exception("Update failed: document not found"));
                    }
                    return Uni.createFrom().item(rowCount);
                });
    }

    public Uni<Integer> delete(UUID id) {
        return client.preparedQuery(String.format("DELETE FROM %s WHERE id=$1", entityData.getTableName()))
                .execute(Tuple.of(id))
                .onItem().transformToUni(rowSet -> {
                    int rowCount = rowSet.rowCount();
                    if (rowCount == 0) {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                    }
                    return Uni.createFrom().item(rowCount);
                });
    }

    private Organization from(Row row) {
        Organization doc = new Organization();
        setDefaultFields(doc, row);
        doc.setIdentifier(row.getString("identifier"));
        doc.setOrgCategory(row.getUUID("org_category_id"));
        doc.setBizID(row.getString("biz_id"));
        doc.setRank(row.getInteger("rank"));
        return doc;
    }
}
