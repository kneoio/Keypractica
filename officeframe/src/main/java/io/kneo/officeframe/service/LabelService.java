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
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }


    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<List<LabelDTO>> getOfCategory(String categoryName, LanguageCode languageCode) {
        Uni<List<Label>> listUni = repository.getOfCategory(categoryName);
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
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
        Uni<Label> labelUni = repository.findById(UUID.fromString(uuid));
        return labelUni.onItem().transform(this::map);
    }

    @Override
    public Uni<Optional<LabelDTO>> getByIdentifier(String identifier) {
        return null;
    }

    public Uni<LabelDTO> getDTOByIdentifier(String identifier, IUser user) {
        Uni<Label> labelUni = repository.findByIdentifier(identifier);
        return labelUni.onItem().transform(this::map);
    }

    private LabelDTO map(Label label) {
        return LabelDTO.builder()
                .author(userRepository.getUserName(label.getAuthor()))
                .regDate(label.getRegDate())
                .lastModifier(userRepository.getUserName(label.getLastModifier()))
                .lastModifiedDate(label.getLastModifiedDate())
                .identifier(label.getIdentifier())
                .localizedName(label.getLocalizedName())
                .category(label.getCategory())
                .parent(label.getParent())
                .color(label.getColor())
                .hidden(label.isHidden())
                .build();
    }

    private LabelDTO mapToDTO(Label doc) {
        return LabelDTO.builder()
                .id(doc.getId())
                .author(userRepository.getUserName(doc.getAuthor()))
                .regDate(doc.getRegDate())
                .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                .lastModifiedDate(doc.getLastModifiedDate())
                .identifier(doc.getIdentifier())
                .color(doc.getColor())
                .category(doc.getCategory())
                .parent(doc.getParent())
                .hidden(doc.isHidden())
                .localizedName(doc.getLocalizedName())
                .build();
    }

    public Uni<Label> findByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier);
    }

    @Override
    public Uni<LabelDTO> add(LabelDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<LabelDTO> update(String id, LabelDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }
}


