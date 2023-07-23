package com.semantyca.core.repository;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.Language;
import com.semantyca.core.repository.exception.DocumentExistsException;
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
import java.util.UUID;

@ApplicationScoped
public class LanguageRepository extends AsyncRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger("LanguageRepository");
    @Inject
    PgPool client;

    @Inject
    ObjectMapper mapper;

    public Uni<List<Language>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM _langs l";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }

        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Uni<Language> findById(UUID uuid) {
        return client.preparedQuery(  "SELECT * FROM _langs WHERE id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Language> findByCode(LanguageCode code) {
        return client.preparedQuery(  "SELECT * FROM _langs WHERE code = $1")
                .execute(Tuple.of(code))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    private Language from(Row row) {
        Map<LanguageCode, String> map;
        try {
            map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return new Language.Builder()
                .setCode(row.getString("code"))
                .setLocalizedNames(map)
                .setOn(row.getBoolean("is_on"))
                .setPosition(999)
                .build();
    }


    public UUID insert(Language node, Long user) throws DocumentExistsException {

        return node.getId();
    }


    public Language update(Language node) {

        return node;
    }

    public int delete(UUID uuid, long id) {

        return 1;
    }
}
