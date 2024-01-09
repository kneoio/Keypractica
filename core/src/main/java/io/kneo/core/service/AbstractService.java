package io.kneo.core.service;

import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.model.DataEntity;
import io.kneo.core.model.embedded.RLS;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.table.EntityData;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractService<T, V> {
    @Inject
    protected UserRepository userRepository;
    @Inject
    protected UserService userService;

    public abstract Uni<V> getDTO(String id);

    protected Uni<List<RLSDTO>> getRLSDTO(AsyncRepository asyncRepository, EntityData entityData, Uni<Optional<T>> secureDataEntityUni, UUID id) {
        Uni<List<RLS>> rlsEntires = secureDataEntityUni.onItem().transformToUni(item ->
                asyncRepository.getAllReaders(id, entityData)
        );

        return rlsEntires.onItem().transform(rlsList -> rlsList.stream()
                .map(this::convertRlSEntries)
                .collect(Collectors.toList()));
    }

    protected RLSDTO convertRlSEntries(RLS rls) {
        return new RLSDTO(userRepository.getUserName(rls.getReader()), rls.getAccessLevel().getAlias(), rls.getReadingTime());
    }

    protected void setDefaultFields(AbstractDTO dto, DataEntity<UUID> doc) {
        dto.setId(doc.getId());
        dto.setAuthor(userService.getUserName(doc.getAuthor()));
        dto.setRegDate(doc.getRegDate());
        dto.setLastModifier(userService.getUserName(doc.getLastModifier()));
        dto.setLastModifiedDate(doc.getLastModifiedDate());
    }


}
