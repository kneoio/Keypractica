package com.semantyca.core.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.user.Role;
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

import java.time.ZoneId;
import java.util.*;


@ApplicationScoped
public class RoleRepository {

    @Inject
    PgPool client;

    @Inject
    ObjectMapper mapper;

    private static final Logger LOGGER = LoggerFactory.getLogger("RoleRepository");

    public Uni<List<Role>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM _roles";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from).collect().asList();
    }

    public Uni<Optional<Role>> findById(UUID uuid) {
        return client.preparedQuery("SELECT * FROM _roles sr WHERE sr.id = $1")
                .execute(Tuple.of(uuid))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<Optional<Role>> findByUserId(long id) {
        return client.preparedQuery("SELECT * FROM _roles sr WHERE sr.user_id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private Role from(Row row) {
        Map<LanguageCode, String> map;
        try {
            Object o =  row.getJsonObject("loc_name");
            if (o != null) {
                map = mapper.readValue(row.getJsonObject("loc_name").toString(), new TypeReference<>() {
                });
            } else {
                map = new HashMap<>();
            }
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return new Role.Builder()
                .setId(row.getUUID("id"))
                .setAuthor(row.getLong("author"))
                .setRegDate(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault()))
                .setLastModifier(row.getLong("last_mod_user"))
                .setLastModifiedDate(row.getLocalDateTime("last_mod_date").atZone(ZoneId.systemDefault()))
                .setIdentifier(row.getString("identifier"))
                .setLocalizedName(map)
                .build();
    }

    public UUID insert(Role node, Long user) {

        return node.getId();
    }


    public Role update(Role node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }
}
