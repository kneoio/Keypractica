package com.semantyca.core.repository;


import com.semantyca.core.model.user.User;
import com.semantyca.core.server.EnvConst;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@ApplicationScoped
public class UserRepository extends Repository {

    @Inject
    PgPool client;

    private static Map<Long, String> userCache = new HashMap();

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
                .onItem().transform(row -> new User.Builder().setLogin(row.getString("login")).build());
    }

    public Uni<User> getId(String login) {
        return client.preparedQuery("SELECT * FROM _users WHERE login = '$1'")
                .execute(Tuple.of(login))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Optional<User>> findById(Long id) {
        return client.preparedQuery("SELECT * FROM _users WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    public Uni<Optional<User>> getName(Long id) {
        return client.preparedQuery("SELECT * FROM _users WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private User from(Row row) {
        User user = new User.Builder()
                .setLogin(row.getString("login"))
                .setEmail(row.getString("email"))
                .setDefaultLang(row.getInteger("default_lang"))
                .setRoles(List.of())
                .setTimeZone(TimeZone.getDefault())
                .build();
        user.setId(row.getLong("id"));
        user.setRegDate(ZonedDateTime.from(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault())));
        return user;
    }

    public Long insert(User node, Long user) {
        return node.getId();
    }

    public User update(User user) {

        return user;
    }

    public int delete(Long id) {
        return 1;
    }
}
