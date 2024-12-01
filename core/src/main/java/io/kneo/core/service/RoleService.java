package io.kneo.core.service;


import io.kneo.core.dto.document.RoleDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.Role;
import io.kneo.core.repository.RoleRepository;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleService extends AbstractService<Role, RoleDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger("RoleService");
    private final RoleRepository repository;

    protected RoleService() {
        super(null, null);
        this.repository = null;
    }

    @Inject
    public RoleService(UserRepository userRepository, UserService userService, RoleRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<RoleDTO>> getAll(final int limit, final int offset) {
        return repository.getAll(limit, offset)
                .chain(list -> {
                    List<Uni<RoleDTO>> unis = list.stream()
                            .map(this::mapToDTO)
                            .collect(Collectors.toList());
                    return Uni.join().all(unis).andFailFast();
                });
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<RoleDTO> getDTO(UUID id, IUser user, LanguageCode language) {
        return repository.findById(id).chain(this::mapToDTO);
    }

    private Uni<RoleDTO> mapToDTO(Role doc) {
        return Uni.combine().all().unis(
                userService.getName(doc.getAuthor()),
                userService.getName(doc.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                RoleDTO.builder()
                        .author(tuple.getItem1())
                        .regDate(doc.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(doc.getLastModifiedDate())
                        .identifier(doc.getIdentifier())
                        .localizedName(doc.getLocalizedName())
                        .localizedDescription(doc.getLocalizedDescription())
                        .build()
        );
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
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
        assert repository != null;
        return repository.update(doc, AnonymousUser.ID);
    }

    public Uni<Integer> delete(String id) {
        assert repository != null;
        return repository.delete(UUID.fromString(id));
    }
}