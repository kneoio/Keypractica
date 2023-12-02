package io.kneo.core.service;

import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Optional;

public interface IRESTService<V> {

    Uni<Integer> getAllCount();

    Uni<List<V>> getAll(int pageSize, int offset);

    Uni<Optional<V>> getByIdentifier(String  identifier);
}
