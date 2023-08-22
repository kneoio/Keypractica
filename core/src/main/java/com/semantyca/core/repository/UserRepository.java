package com.semantyca.core.repository;


import com.semantyca.core.model.user.IUser;
import com.semantyca.core.model.user.UndefinedUser;
import com.semantyca.core.model.user.User;
import com.semantyca.core.server.EnvConst;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserRepository extends AsyncRepository {

    @Inject
    PgPool client;

    private static Map<Long, IUser> userCache = new HashMap<>();
    private static final Map<String, IUser> userAltCache = new HashMap<>();


    void onStart(@Observes StartupEvent ev) {
        userCache = getAll().onItem().transform(users -> users.stream().filter(u -> u.getUserId() != null)
                        .collect(Collectors.toMap(IUser::getUserId, user -> user)))
                .await().indefinitely();
        userAltCache.putAll(userCache.values().stream()
                .collect(Collectors.toMap(IUser::getUserName, Function.identity())));
       /* userAltCache.putAll(userCache.values().stream()
                .collect(Collectors.toMap(IUser::getEmail, Function.identity())));*/
    }

    public Uni<List<IUser>> getAll() {
        return client.query(String.format("SELECT * FROM _users LIMIT %d OFFSET 0", 100))
                .execute()
                .onItem().transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem().transform(this::from)
                .collect().asList();
    }

    public Multi<IUser> getAllStream() {
        return client.query(String.format("SELECT * FROM _users LIMIT %d OFFSET 0", EnvConst.DEFAULT_PAGE_SIZE))
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().call(row -> Uni.createFrom().item(row).onItem().delayIt().by(Duration.ofMillis(100)))
                .onItem().transform(row -> new User.Builder().setLogin(row.getString("login")).build());
    }

    public Uni<IUser> getId(String login) {
        return client.preparedQuery("SELECT * FROM _users WHERE login = '$1'")
                .execute(Tuple.of(login))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Optional<IUser>> findById(Long id) {
        IUser user = userCache.get(id);
        if (user == null) {
            return client.preparedQuery("SELECT * FROM _users WHERE id = $1")
                    .execute(Tuple.of(id))
                    .onItem().transform(RowSet::iterator)
                    .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
        } else {
            return Uni.createFrom().item(user)
                    .onItem().transform(Optional::ofNullable);
        }
    }

    public Optional<IUser> findByLogin(String userName) {
        if (userName == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(userAltCache.get(userName));
        }
        /*return client.preparedQuery("SELECT * FROM _users WHERE login = '$1'")
                .execute(Tuple.of(userName))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(fromShort(iterator.next())) : Optional.of(UndefinedUser.Build()))
                .await().indefinitely();*/
    }

    public String getUserName(long id) {
        return userCache.getOrDefault(id, UndefinedUser.Build()).getUserName();
    }

    public Uni<Optional<IUser>> getName(Long id) {
        return client.preparedQuery("SELECT * FROM _users WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? Optional.of(from(iterator.next())) : Optional.empty());
    }

    private IUser from(Row row) {
        User user = new User.Builder()
                .setLogin(row.getString("login"))
                .setEmail(row.getString("email"))
                .setDefaultLang(row.getInteger("default_lang"))
                .setRoles(List.of())
                .setTimeZone(TimeZone.getDefault())
                .build();
        user.setId(row.getLong("id"));
        user.setRegDate(ZonedDateTime.from(row.getLocalDateTime("reg_date").atZone(ZoneId.systemDefault())));
        userCache.put(row.getLong("id"), user);
        return user;
    }

    public Uni<Long> insert(User user) {
        String sql = "INSERT INTO _users (name, email) VALUES ($2, $3) RETURNING id";
        Tuple params = Tuple.of(user.getUserName(), user.getEmail());

        Uni<Long> longUni = client.preparedQuery(sql)
                .execute(params)
                .onItem().transform(result -> result.iterator().next().getLong("id"));
        userCache.clear();
        return longUni;
    }

    public User update(User user) {
        userCache.clear();
        return user;
    }

    public int delete(Long id) {
        userCache.clear();
        return 1;
    }


}
