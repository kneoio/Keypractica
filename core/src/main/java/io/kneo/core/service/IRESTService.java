package io.kneo.core.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.UUID;

public interface IRESTService<V> {

    Uni<Integer> getAllCount();

    Uni<List<V>> getAll(int pageSize, int offset, LanguageCode languageCode);

    Uni<V> getDTO(UUID id, IUser user, LanguageCode language);

    Uni<V> getDTOByIdentifier(String  identifier);

    Uni<V> upsert(UUID id, V dto, IUser user, LanguageCode code);
}
