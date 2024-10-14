package io.kneo.core.service;

import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.DataEntity;
import io.kneo.core.model.embedded.RLS;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.smallrye.mutiny.Uni;

import java.util.UUID;

public abstract class AbstractService<T, V> {
    protected UserRepository userRepository;
    protected UserService userService;

    public AbstractService() {

    }

    public AbstractService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public abstract Uni<V> getDTO(UUID id, IUser user, LanguageCode language);

    public Uni<V> upsert(String id, V dto, IUser user, LanguageCode code) throws DocumentModificationAccessException {
         return Uni.createFrom().failure(new RuntimeException("The upsert is not implemented"));
    };

    public abstract Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException;


    protected RLSDTO convertRlSEntries(RLS rls) {
        return new RLSDTO(userRepository.getUserName(rls.getReader()), rls.getAccessLevel().getAlias(), rls.getReadingTime());
    }

    protected void setDefaultFields(AbstractDTO dto, DataEntity<UUID> doc) {
        dto.setId(doc.getId());
        dto.setAuthor(userService.getName(doc.getAuthor()));
        dto.setRegDate(doc.getRegDate());
        dto.setLastModifier(userService.getName(doc.getLastModifier()));
        dto.setLastModifiedDate(doc.getLastModifiedDate());
    }
}