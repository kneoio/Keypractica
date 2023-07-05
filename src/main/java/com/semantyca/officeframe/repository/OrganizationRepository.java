package com.semantyca.officeframe.repository;

import com.semantyca.model.Language;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.model.constants.ProjectStatusType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    PgPool client;

    public Uni<List<Project>> getAll(final int limit, final int offset, final long userID) {


        String sql = "SELECT * FROM prj__projects p, prj__project_readers ppr WHERE p.id = ppr.entity_id AND ppr.readers = " + userID;
        if (limit > 0 ) {
            sql += String.format(" LIMIT %s OFFSET %s", limit, offset);
        }

        return client.query(sql)
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new Project.Builder()
                        .setName(row.getString("name"))
                        .setFinishDate(row.getLocalDate("finishdate"))
                        .setStatus(ProjectStatusType.valueOf(row.getString("status")))
                        .setPosition(999)
                        //.setCoder(row.getString("programmer"))
                        //.setTester(row.getString("tester"))
                        .setComment(row.getString("comment"))
                        .build())
                        .collect().asList();
    }

    private static <Project> List<Project> resultList(Iterable<Project> result) {
        ArrayList<Project> list = new ArrayList<>();
        result.forEach(list::add);
        return list;
    }

    public Project findById(UUID uuid) {
        return null;
    }

    public Optional<Project> findByValue(String base) {
        return null;
    }

    public String insert(Project node, Long user) {

        return node.getId();
    }


    public Language update(Language node) {

        return node;
    }

    public int delete(Long id) {

        return 1;
    }
}
