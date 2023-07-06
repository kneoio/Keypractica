package com.semantyca.repository;


import com.semantyca.model.Language;
import com.semantyca.model.Module;
import com.semantyca.repository.exception.DocumentExistsException;
import com.semantyca.server.EnvConst;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ModuleRepository {

    @Inject
    PgPool client;

    public Uni<List<Module>> getAll() {
        return client.query(String.format("SELECT * FROM _modules LIMIT %d OFFSET 0", EnvConst.DEFAULT_PAGE_SIZE))
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new Module.Builder().setName(row.getString("name")).build())
                .collect().asList();
    }

    public Language findById(UUID uuid) {

        return null;
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
