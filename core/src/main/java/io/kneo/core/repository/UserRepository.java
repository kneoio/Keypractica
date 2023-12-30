package io.kneo.core.repository;


import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.Role;
import io.kneo.core.model.user.UndefinedUser;
import io.kneo.core.model.user.User;
import io.kneo.core.server.EnvConst;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserRepository extends AsyncRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger("UserRepository");


    @Inject
    PgPool client;

    private static Map<Long, IUser> userCache = new HashMap<>();
    private static final Map<String, IUser> userAltCache = new HashMap<>();


    void onStart(@Observes StartupEvent ev) {
        userCache = getAll().onItem().transform(users -> users.stream().filter(u -> u.getId() != null)
                        .collect(Collectors.toMap(IUser::getId, user -> user)))
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

    public Uni<Optional<IUser>> get(Long id) {
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

    public Optional<IUser> findById(long id) {
         return Optional.ofNullable(userCache.get(id));
        //TODO it needs to initialize the cache
    }
    public Optional<IUser> findByLogin(String userName) {
        if (userName == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(userAltCache.get(userName));
        }
        //TODO it needs to initialize the cache
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
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        String sql = "INSERT INTO _users (default_lang, email, i_su, login, reg_date, status, confirmation_code)VALUES($1, $2, $3, $4, $5, $6, $7) RETURNING id";
        String modulesSQL = "INSERT INTO _user_modules (module_id, user_id, is_on) VALUES($1, $2, $3)";
        String rolesSQL = "INSERT INTO _user_roles (role_id, user_id, is_on) VALUES($1, $2, $3)";
        Tuple params = Tuple.of(user.getDefaultLang(), user.getEmail(), user.isSupervisor(), user.getLogin(), localDateTime);
        Tuple finalParams = params.addValue(user.getRegStatus()).addInteger(user.getConfirmationCode());
        return client.withTransaction(tx -> tx.preparedQuery(sql)
                .execute(finalParams)
                .onItem().transform(result -> result.iterator().next().getLong("id"))
                .onItem().transformToUni(id -> {
                    List<Uni<Integer>> userModulesList = new ArrayList<>();
                    for (Module module : user.getModules()) {
                        userModulesList.add(tx.preparedQuery(modulesSQL)
                                .execute(Tuple.of(module.getId(), id, true))
                                .onItem().transform(SqlResult::rowCount));
                    }
                    if (userModulesList.isEmpty()) {
                        return Uni.createFrom().item(id);
                    } else {
                        return Uni.combine().all().unis(userModulesList).combinedWith(results -> id);
                    }
                })
                .onItem().transformToUni(id -> {
                    List<Uni<Integer>> userRolesList = new ArrayList<>();
                    for (Role role : user.getRoles()) {
                        userRolesList.add(tx.preparedQuery(rolesSQL)
                                .execute(Tuple.of(role.getId(), id, true))
                                .onItem().transform(SqlResult::rowCount));
                    }
                    if (userRolesList.isEmpty()) {
                        return Uni.createFrom().item(id);
                    } else {
                        userCache.clear();
                        userAltCache.clear();
                        return Uni.combine().all().unis(userRolesList).combinedWith(results -> id);
                    }
                }).onFailure().recoverWithUni(throwable -> {
                    LOGGER.error(throwable.getMessage(), throwable);
                    return Uni.createFrom().failure(new RuntimeException("Failed to insert user, roles or modules", throwable));
                }));
    }

    public Uni<Long> update(User user) {
        String sql = "UPDATE _users SET default_lang=$1, email='', i_su=$2, status=$3, ui_theme=$4, time_zone=0 WHERE id=$5";
        Tuple params = Tuple.of(user.getDefaultLang(), user.getEmail(), user.isSupervisor(), user.getLogin());
        params = params.addValue(user.getRegStatus()).addValue("cinzento").addInteger(user.getConfirmationCode());

        Uni<Long> longUni = client.preparedQuery(sql)
                .execute(params)
                .onItem().transform(result -> result.iterator().next().getLong("id"));
        userCache.clear();
        userAltCache.clear();
        return longUni;
    }

    public int delete(Long id) {
        userCache.clear();
        userAltCache.clear();
        return 1;
    }


}
