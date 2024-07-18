package io.kneo.core.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

public interface IRESTService<V> {

    Uni<Integer> getAllCount();

    Uni<List<V>> getAll(int pageSize, int offset, LanguageCode languageCode);

    Uni<V> getDTO(String id, IUser user, LanguageCode language);



    Uni<Optional<V>> getByIdentifier(String  identifier);

    Uni<V> upsert(String id, V dto, IUser user);
}
