package io.kneo.officeframe.service;

import io.kneo.core.model.user.IUser;
import io.kneo.core.service.AbstractService;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.model.Label;
import io.kneo.officeframe.repository.LabelRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LabelService extends AbstractService<Label, LabelDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger("LabelService");
    @Inject
    private LabelRepository repository;

    public Uni<List<LabelDTO>> getAll(final int limit, final int offset) {
        Uni<List<Label>> taskUni = repository.getAll(limit, offset);
        return taskUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(e ->
                                LabelDTO.builder()
                                        .id(e.getId())
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .identifier(e.getIdentifier())
                                        .color(e.getColor())
                                        .category(e.getCategory())
                                        //.parent(e.getParent())
                                        .hidden(e.isHidden())
                                        .build())
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }


    public Uni<LabelDTO> get(String uuid) {
        return get(UUID.fromString(uuid));
    }

    public Uni<LabelDTO> get(UUID uuid) {
       return null;
    }

    public Uni<Object> add(LabelDTO dto) {
        return null;
    }

    public IUser update(LabelDTO dto) {
        return null;
    }
}