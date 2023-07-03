package com.semantyca.repository;

import com.semantyca.model.user.User;
import com.semantyca.server.EnvConst;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository {

    @Inject
    PgPool client;


    public Multi<User> getAllUsers() {
        return client.query(String.format("SELECT * FROM _users LIMIT %d OFFSET 0", EnvConst.DEFAULT_PAGE_SIZE))
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().call(row -> Uni.createFrom().item(row).onItem().delayIt().by(Duration.ofMillis(100)))
                .onItem().transform(row -> new User.Builder().setLogin (row.getString("login")).build());
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
