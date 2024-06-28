package io.kneo.officeframe.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.table.EntityData;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.repository.table.OfficeFrameNameResolver;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.kneo.officeframe.repository.table.OfficeFrameNameResolver.POSITION;

@ApplicationScoped
public class PositionRepository extends AsyncRepository {
    private static final EntityData entityData = OfficeFrameNameResolver.create().getEntityNames(POSITION);

    public Uni<List<Position>> getAll(final int limit, final int offset) {
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

    public Uni<Optional<Position>> findById(UUID uuid) {
        return findById(uuid, entityData, this::from);
    }

    private Position from(Row row) {
        EnumMap<LanguageCode, String> map;
        try {
            map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        Position doc = new Position();
        doc.setId(row.getUUID("id"));
        doc.setAuthor(row.getLong("author"));
        doc.setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()));
        doc.setLastModifier(row.getLong("last_mod_user"));
        doc.setRegDate(row.getLocalDateTime("last_mod_date").atZone(ZoneId.systemDefault()));
        doc.setIdentifier(row.getString("identifier"));
        doc.setLocalizedName(map);
        return doc;
    }



}
