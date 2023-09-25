package com.semantyca.core.service;

import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.core.repository.AsyncRepository;
import com.semantyca.core.repository.UserRepository;
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

    public abstract Uni<V> get(String id);

    protected Uni<List<RLSDTO>> getRLSDTO(AsyncRepository asyncRepository, Uni<Optional<T>> secureDataEntityUni, UUID id) {
        Uni<List<RLS>> rlsEntires = secureDataEntityUni.onItem().transformToUni(item ->
                asyncRepository.getAllReaders(id)
        );

        return rlsEntires.onItem().transform(rlsList -> rlsList.stream()
                .map(this::convertRlSEntries)
                .collect(Collectors.toList()));
    }


    protected RLSDTO convertRlSEntries(RLS rls) {
        return new RLSDTO(userRepository.getUserName(rls.getReader()), rls.getAccessLevel().getAlias(), rls.getReadingTime());
    }

}
