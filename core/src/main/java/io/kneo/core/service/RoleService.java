package io.kneo.core.service;


import io.kneo.core.dto.document.RoleDTO;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.Role;
import io.kneo.core.repository.RoleRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService extends AbstractService<Role, RoleDTO> {
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

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<RoleDTO> getDTO(String id, IUser user) {
        Uni<Optional<Role>> uni = repository.findById(UUID.fromString(id));
        return uni.onItem().transform(optional -> {
            Role doc = optional.orElseThrow();
            RoleDTO dto = new RoleDTO();
            setDefaultFields(dto, doc);
            dto.setIdentifier(doc.getIdentifier());
            dto.setLocalizedName(doc.getLocalizedName());
            dto.setLocalizedDescription(doc.getLocalizedDescription());
            return dto;
        });
    }

    public Uni<UUID> add(RoleDTO dto) {
        Role doc = new Role.Builder()
                .setIdentifier(dto.getIdentifier())
                .setLocalizedName(dto.getLocalizedName())
                .setLocalizedDescription(dto.getLocalizedDescription())
                .build();
        return repository.insert(doc, AnonymousUser.ID);
    }

    public Uni<Integer> update(String id, RoleDTO dto) {
        Role doc = new Role.Builder()
                .setId(UUID.fromString(id))
                .setIdentifier(dto.getIdentifier())
                .setLocalizedName(dto.getLocalizedName())
                .build();
        return repository.update(doc, AnonymousUser.ID);
    }

    public Uni<Void> delete(String id) {
        return repository.delete(UUID.fromString(id));
    }
}
