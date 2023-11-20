package io.kneo.core.service;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface IBasicService<T> {
    Uni<List<T>> getAll(int i, int i1);
}
