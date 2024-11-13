package io.kneo.core.service;

import io.kneo.core.dto.cnst.UserRegStatus;
import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.model.Module;
import io.kneo.core.model.SimpleReferenceEntity;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.Role;
import io.kneo.core.model.user.User;
import io.kneo.core.repository.ModuleRepository;
import io.kneo.core.repository.RoleRepository;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.exception.ServiceException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger("UserService");
    @Inject
    private UserRepository repository;
    @Inject
    private RoleService roleService;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private ModuleRepository moduleRepository;

    public Uni<List<IUser>> getAll() {
        return repository.getAll();
    }

    public Uni<List<IUser>> search(String keyword) {
        return repository.search(keyword);
    }

    public Multi<IUser> getAllStream() {
        return repository.getAllStream();
    }

    public Uni<Optional<IUser>> get(String id) {
        return repository.get(Long.parseLong(id));
    }

    public Uni<Optional<IUser>> get(long id) {
        return repository.get(id);
    }

    public Uni<Long> resolveIdentifier(String identifier) {
        return repository.findByIdentifier(identifier);
    }

    public Uni<IUser> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    public Uni<Optional<IUser>> findById(long id) {
        return repository.findById(id);
    }

    public Uni<String> getName(long id) {
        return repository.getUserName(id);
    }

    public Uni<String> getUserName(long id) {
        return repository.getUserName(id);
    }

    public Uni<Long> add(UserDTO dto) {
        User user = new User.Builder()
                .setLogin(dto.getLogin())
                .setEmail(dto.getEmail())
                .setRegStatus(UserRegStatus.REGISTERED)
                .build();
        Uni<List<Role>> rolesUni = roleRepository.getAll(0, 1000);
        Uni<List<Module>> moduleUni = moduleRepository.getAll(0, 1000);

        return rolesUni.onItem().transformToUni(roles -> {
            user.setRoles(getAllValidReferences(roles, dto.getRoles()));
            return moduleUni;
        }).onFailure().recoverWithUni(failure -> {
            throw new ServiceException(failure);
        }).onItem().transformToUni(modules -> {
            try {
                user.setModules(getAllValidReferences(modules, dto.getModules()));
                return repository.insert(user);
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        });
    }

    public Uni<Long> update(String id, UserDTO userDTO) {
        User user = new User.Builder()
                .setLogin(userDTO.getLogin())
                .setEmail(userDTO.getEmail())
                .build();

        return repository.insert(user);
    }

    private <T extends SimpleReferenceEntity> List<T> getAllValidReferences(List<T> allAvailable, List<String> provided) {
        List<T> allValidRoles = new ArrayList<>();
        for (T e : allAvailable) {
            if (provided.contains(e.getIdentifier())) {
                allValidRoles.add(e);
            }
        }
        return allValidRoles;
    }

    public Uni<Long> delete(String id) {
        assert repository != null;
        return repository.delete(Long.valueOf(id));
    }
}
