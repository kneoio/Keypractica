package io.kneo.core.service;

import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.DataEntity;
import io.kneo.core.model.embedded.RLS;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.AsyncRepository;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.repository.table.EntityData;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractService<T, V> {
    protected UserRepository userRepository;
    protected UserService userService;

    public AbstractService() {

    }

    public AbstractService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public abstract Uni<V> getDTO(String id, IUser user, LanguageCode language);
    public abstract Uni<UUID> add(V dto, IUser user);
    public abstract Uni<Integer> update(String id, V dto, IUser user) throws DocumentModificationAccessException;
    public abstract Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException;

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