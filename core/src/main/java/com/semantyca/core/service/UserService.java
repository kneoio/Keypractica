package com.semantyca.core.service;

import com.semantyca.core.dto.cnst.UserRegStatus;
import com.semantyca.core.dto.document.UserDTO;
import com.semantyca.core.model.Module;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.model.user.Role;
import com.semantyca.core.model.user.User;
import com.semantyca.core.repository.ModuleRepository;
import com.semantyca.core.repository.RoleRepository;
import com.semantyca.core.repository.UserRepository;
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

    public Multi<IUser> getAllStream() {
        return repository.getAllStream();
    }

    public Uni<Optional<IUser>> get(String id) {
        return repository.findById(Long.parseLong(id));
    }

    public String getUserName(long id) {
        return repository.getUserName(id);
    }

    public Uni<Long> add(UserDTO userDTO) {
        User user = new User.Builder()
                .setLogin(userDTO.login())
                .setPwd(userDTO.pwd())
                .setEmail(userDTO.email())
                .setRegStatus(UserRegStatus.REGISTERED)
                .build();
        Uni<List<Role>> rolesUni = roleRepository.getAll(0, 1000);
        Uni<List<Module>> moduleUni = moduleRepository.getAll(0, 1000);

        return rolesUni.onItem().transformToUni(roles -> {
            user.setRoles(getAllValidReferences(roles, userDTO.roles()));
            return moduleUni;
        }).onItem().transformToUni(modules -> {
            user.setModules(getAllValidReferences(modules, userDTO.modules()));
            return repository.insert(user);
        });
    }

    public Uni<Long> update(UserDTO userDTO) {
        User user = new User.Builder()
                .setLogin(userDTO.login())
                .setPwd(userDTO.pwd())
                .setEmail(userDTO.email())
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

}
