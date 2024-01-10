package io.kneo.officeframe.service;

import io.kneo.core.model.user.IUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.repository.PositionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PositionService extends AbstractService<Position, PositionDTO> implements IRESTService<PositionDTO> {
    @Inject
    private PositionRepository repository;

    public Uni<List<PositionDTO>> getAll(final int limit, final int offset) {
        Uni<List<Position>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(doc ->
                                PositionDTO.builder()
                                        .id(doc.getId())
                                        .author(userRepository.getUserName(doc.getAuthor()))
                                        .regDate(doc.getRegDate())
                                        .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                                        .lastModifiedDate(doc.getLastModifiedDate())
                                        .identifier(doc.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<Optional<PositionDTO>> getByIdentifier(String identifier) {
        return null;
    }

    public Uni<PositionDTO> getDTO(String uuid, IUser user) {
        return getDTO(UUID.fromString(uuid));
    }

    public Uni<PositionDTO> getDTO(UUID uuid) {
        Uni<Optional<Position>> uni = get(uuid);
        return uni.onItem().transform(docOpt -> {
            Position doc = docOpt.orElseThrow();
            return PositionDTO.builder()
                    .author(userRepository.getUserName(doc.getAuthor()))
                    .regDate(doc.getRegDate())
                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .identifier(doc.getIdentifier())
                    .build();
        });
    }

    public Uni<Optional<Position>> get(UUID uuid) {
        return repository.findById(uuid);
    }

}
