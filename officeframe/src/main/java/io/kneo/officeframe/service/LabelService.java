package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.model.Label;
import io.kneo.officeframe.repository.LabelRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LabelService extends AbstractService<Label, LabelDTO> implements IRESTService<LabelDTO> {
    private final LabelRepository repository;

    @Inject
    public LabelService(UserRepository userRepository, UserService userService, LabelRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<LabelDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
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

    @Override
    public Uni<Optional<LabelDTO>> getByIdentifier(String identifier) {
        return null;
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<List<LabelDTO>> getLabels(UUID id, String type) {
        Uni<List<Label>> labelsUni = repository.findForDocument(id, type);
        return labelsUni.onItem().transformToUni(labels ->
                Uni.createFrom().item(
                        labels.stream().map(e ->
                                        LabelDTO.builder()
                                                .identifier(e.getIdentifier())
                                                .color(e.getColor())
                                                .category(e.getCategory())
                                                .hidden(e.isHidden())
                                                .build())
                                .collect(Collectors.toList())));
    }


    public Uni<LabelDTO> getDTO(String uuid, IUser user, LanguageCode language) {
        Uni<Optional<Label>> labelUni = repository.findById(UUID.fromString(uuid));
        return labelUni.onItem().transform(this::map);
    }

    public Uni<LabelDTO> getDTOByIdentifier(String identifier, IUser user) {
        Uni<Optional<Label>> labelUni = repository.findByIdentifier(identifier);
        return labelUni.onItem().transform(this::map);
    }

    private LabelDTO map(Optional<Label> labelOpt) {
        Label label = labelOpt.get();
        return LabelDTO.builder()
                .author(userRepository.getUserName(label.getAuthor()))
                .regDate(label.getRegDate())
                .lastModifier(userRepository.getUserName(label.getLastModifier()))
                .lastModifiedDate(label.getLastModifiedDate())
                .identifier(label.getIdentifier())
                .build();
    }

    public Uni<Optional<Label>> findByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier);
    }

    @Override
    public Uni<UUID> add(LabelDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> update(String id, LabelDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }


}
