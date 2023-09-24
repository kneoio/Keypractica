package com.semantyca.core.service;


import com.semantyca.core.dto.RoleDTO;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.Role;
import com.semantyca.core.repository.RoleRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService {
    private static final Logger LOGGER = LoggerFactory.getLogger("RoleService");
    @Inject
    private RoleRepository repository;
    @Inject
    private UserService userService;

    public Uni<List<RoleDTO>> getAll(final int limit, final int offset) {
        Uni<List<Role>> roleListUni = repository.getAll(limit, offset);
        return roleListUni
                .onItem().transform(roleStream -> roleStream.stream()
                        .map(role ->
                                RoleDTO.builder()
                                        .author(userService.getUserName(role.getAuthor()))
                                        .regDate(role.getRegDate())
                                        .lastModifier(userService.getUserName(role.getLastModifier()))
                                        .lastModifiedDate(role.getLastModifiedDate())
                                        .identifier(role.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }


    public Uni<Optional<RoleDTO>> get(String id) {
       // return repository.findById(UUID.fromString(id));
        return null;
    }

    public String  add(RoleDTO dto) {
        Role node = new Role.Builder()
               // .setName(dto.name())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Role update(RoleDTO dto) {
        Role user = new Role.Builder()
            //    .setCode(dto.code())
                .build();
        return repository.update(user);
    }
}
