package com.semantyca.core.service;

import com.semantyca.core.dto.document.UserDTO;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.model.user.User;
import com.semantyca.core.repository.UserRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger("UserService");
    @Inject
    private UserRepository repository;

    public Uni<List<IUser>> getAll() {
        return repository.getAll();
    }

    public Multi<IUser> getAllStream() {
        return repository.getAllStream();
    }

    public Uni<Optional<IUser>> get(String id) {
        return repository.findById(Long.parseLong(id));
    }

    public String getUserName(long id) {
        return repository.getUserName(id);
    }
    public Long add(UserDTO userDTO) {
        User user = new User.Builder()
                .setLogin(userDTO.login())
                .setPwd(userDTO.pwd())
                .setEmail(userDTO.email())
                .build();
        return repository.insert(user, AnonymousUser.ID);
    }

    public User update(UserDTO userDTO) {
        User user = new User.Builder()
                .setLogin(userDTO.login())
                .setPwd(userDTO.pwd())
                .setEmail(userDTO.email())
                .build();
        return repository.update(user);
    }
}
