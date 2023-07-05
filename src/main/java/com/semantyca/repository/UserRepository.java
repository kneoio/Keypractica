package com.semantyca.repository;

import com.semantyca.model.user.User;
import com.semantyca.server.EnvConst;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.*;

@ApplicationScoped
public class UserRepository {

    @Inject
    PgPool client;

    private static Map<String, Long> userCache = new HashMap();

    public Uni<List<User>> getAll() {
        return client.query(String.format("SELECT * FROM _users LIMIT %d OFFSET 0", EnvConst.DEFAULT_PAGE_SIZE))
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(row -> new User.Builder().setLogin(row.getString("login")).build())
                .collect().asList();
    }

    public Multi<User> getAllStream() {
        return client.query(String.format("SELECT * FROM _users LIMIT %d OFFSET 0", EnvConst.DEFAULT_PAGE_SIZE))
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().call(row -> Uni.createFrom().item(row).onItem().delayIt().by(Duration.ofMillis(100)))
                .onItem().transform(row -> new User.Builder().setLogin (row.getString("login")).build());
    }
    public Long getId(String login) {
        Long id =  userCache.get(login);
        if (id == null) {
          /*  return Uni.createFrom().completionStage(client.preparedQuery("SELECT id FROM _users WHERE login = \$1", Tuple.of(username))
                            .thenApply(pgRowSet -> {
                                RowIterator<Row> iterator = pgRowSet.iterator();
                                if (iterator.hasNext()) {
                                    return iterator.next().getLong("id");
                                } else {
                                    return null;
                                }
                            })
            );*/
            userCache.put(login, id);
        }
        return id;
    }


    public User findById(UUID uuid) {
        return null;
    }

    public Optional<User> findByValue(String base) {
        return null;
    }

    public Long insert(User node, Long user) {

        return node.getIdentifier();
    }


    public User update(User user) {

        return user;
    }

    public int delete(Long id) {
        return 1;
    }
}
