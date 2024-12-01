package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.repository.PositionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PositionService extends AbstractService<Position, PositionDTO> implements IRESTService<PositionDTO> {
    private final PositionRepository repository;

    public PositionService(UserRepository userRepository, UserService userService, PositionRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<PositionDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        return repository.getAll(limit, offset)
                .chain(list -> Uni.join().all(
                        list.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList())
                ).andFailFast());
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<PositionDTO> getDTOByIdentifier(String identifier) {
        return null;
    }

    public Uni<PositionDTO> getDTO(UUID uuid, IUser user, LanguageCode language) {
        return getDTO(uuid);
    }

    @Override
    public Uni<PositionDTO> upsert(String id, PositionDTO dto, IUser user, LanguageCode code) {
        Position doc = new Position();
        doc.setIdentifier(dto.getIdentifier());
        doc.setLocalizedName(dto.getLocalizedName());
        return null;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }

    public Uni<PositionDTO> getDTO(UUID uuid) {
        return repository.findById(uuid).chain(this::mapToDTO);
    }

    private Uni<PositionDTO> mapToDTO(Position doc) {
        return Uni.combine().all().unis(
                userRepository.getUserName(doc.getAuthor()),
                userRepository.getUserName(doc.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                PositionDTO.builder()
                        .author(tuple.getItem1())
                        .regDate(doc.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(doc.getLastModifiedDate())
                        .identifier(doc.getIdentifier())
                        .localizedName(doc.getLocalizedName())
                        .build()
        );
    }

    public Uni<Position> get(UUID uuid) {
        return repository.findById(uuid);
    }
}