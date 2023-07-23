package com.semantyca.core.service;

import com.semantyca.core.dto.IDTO;
import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.model.DataEntity;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.core.repository.AsyncRepository;
import com.semantyca.core.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractService<T> {

    @Inject
    protected UserRepository userRepository;


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

    protected void fillCommonFileds(DataEntity dataEntity, IDTO dto) {
          dto.setAuthor(userRepository.getUserName(dataEntity.getAuthor()));
          dto.setRegDate(dataEntity.getRegDate());
          dto.setLastModifier(userRepository.getUserName(dataEntity.getLastModifier()));
          dto.setLastModifiedDate(dataEntity.getLastModifiedDate());
    }

}
