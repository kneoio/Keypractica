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

import java.time.Duration;
import java.util.UUID;

public abstract class AbstractService<T, V> {
    protected static final Duration TIMEOUT = Duration.ofSeconds(5);
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
        return new RLSDTO(userRepository.getUserName(rls.getReader()).await().atMost(TIMEOUT), rls.getAccessLevel().getAlias(), rls.getReadingTime());
    }

    protected void setDefaultFields(AbstractDTO dto, DataEntity<UUID> doc) {
        dto.setId(doc.getId());
        dto.setAuthor(userService.getName(doc.getAuthor()).await().atMost(TIMEOUT));
        dto.setRegDate(doc.getRegDate());
        dto.setLastModifier(userService.getName(doc.getLastModifier()).await().atMost(TIMEOUT));
        dto.setLastModifiedDate(doc.getLastModifiedDate());
    }
}