package io.kneo.officeframe.repository;

import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Organization;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    PgPool client;

    public Uni<List<OrganizationDTO>> getAll(final int limit, final int offset) {
        String sql = "SELECT * FROM staff__orgs ORDER BY rank";
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }
        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new OrganizationDTO(row.getString("name"), row.getString("biz_id"))).collect().asList();
    }

    public Organization findById(UUID uuid) {
        return null;
    }

    public Optional<Organization> findByValue(String base) {
        return null;
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
