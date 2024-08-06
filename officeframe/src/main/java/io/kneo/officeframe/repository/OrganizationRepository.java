package io.kneo.officeframe.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.ORGANIZATION;

@ApplicationScoped
public class OrganizationRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(ORGANIZATION);

    private static final String COLUMN_ORG_CATEGORY_ID = "org_category_id";
    private static final String COLUMN_BIZ_ID = "biz_id";

    @Inject
    public OrganizationRepository(PgPool client, ObjectMapper mapper) {
        super(client, mapper, null);
    }


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

    public Uni<List<Organization>> getAllPrimary() {
        String sql = String.format("SELECT * FROM %s t WHERE t.is_primary = true ORDER BY rank", entityData.getTableName());
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Organization> findById(UUID uuid) {
        return client.preparedQuery(String.format("SELECT * FROM %s WHERE id = $1", entityData.getTableName()))
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> {
                    if (iterator.hasNext()) {
                        return from(iterator.next());
                    } else {
                        return null;
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

    public Uni<Organization> insert(Organization doc, IUser user) {
        String sql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9) RETURNING id",
                entityData.getTableName(),
                COLUMN_AUTHOR,
                COLUMN_REG_DATE,
                COLUMN_LAST_MOD_USER,
                COLUMN_LAST_MOD_DATE,
                COLUMN_IDENTIFIER,
                COLUMN_ORG_CATEGORY_ID,
                COLUMN_BIZ_ID,
                COLUMN_RANK,
                COLUMN_LOCALIZED_NAME);

        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
        LocalDateTime now = LocalDateTime.now();

        Tuple params = Tuple.of(user.getId(), now, user.getId(), now)
                .addString(doc.getIdentifier())
                .addUUID(doc.getOrgCategory())
                .addString(doc.getBizID())
                .addInteger(doc.getRank())
                .addJsonObject(localizedNameJson);

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transformToUni(result -> {
                    UUID id = result.iterator().next().getUUID("id");
                    return findById(id).onItem()
                            .transform(optionalOrg -> optionalOrg);
                });
    }

    public Uni<Organization> update(UUID id, Organization doc, IUser user) {
        String sql = String.format("UPDATE %s SET %s=$1, %s=$2, %s=$3, %s=$4, %s=$5, %s=$6, %s=$7 WHERE id=$8",
                entityData.getTableName(),
                COLUMN_LAST_MOD_USER,
                COLUMN_LAST_MOD_DATE,
                COLUMN_IDENTIFIER,
                COLUMN_ORG_CATEGORY_ID,
                COLUMN_BIZ_ID,
                COLUMN_RANK,
                COLUMN_LOCALIZED_NAME);

        JsonObject localizedNameJson = JsonObject.mapFrom(doc.getLocalizedName());
        LocalDateTime now = LocalDateTime.now();

        Tuple params = Tuple.of(user.getId(), now)
                .addString(doc.getIdentifier())
                .addUUID(doc.getOrgCategory())
                .addString(doc.getBizID())
                .addInteger(doc.getRank())
                .addJsonObject(localizedNameJson)
                .addUUID(id);

        return client.preparedQuery(sql)
                .execute(params)
                .onItem().transformToUni(rowSet -> {
                    if (rowSet.rowCount() == 0) {
                        return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
                    }
                    return findById(id);
                })
                .onItem().transform(organization -> organization);
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
        doc.setIdentifier(row.getString(COLUMN_IDENTIFIER));
        doc.setOrgCategory(row.getUUID(COLUMN_ORG_CATEGORY_ID));
        doc.setBizID(row.getString(COLUMN_BIZ_ID));
        doc.setRank(row.getInteger(COLUMN_RANK));

        JsonObject localizedNameJson = row.getJsonObject(COLUMN_LOCALIZED_NAME);
        if (localizedNameJson != null) {
            EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
            localizedNameJson.getMap().forEach((key, value) -> localizedName.put(LanguageCode.valueOf(key), (String) value));
            doc.setLocalizedName(localizedName);
        }

        return doc;
    }
}
