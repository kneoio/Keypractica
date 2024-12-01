package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.TaskTypeDTO;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.repository.TaskTypeRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskTypeService extends AbstractService<TaskType, TaskTypeDTO> implements IRESTService<TaskTypeDTO> {
    private final TaskTypeRepository repository;

    @Inject
    public TaskTypeService(UserRepository userRepository, UserService userService, TaskTypeRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<TaskTypeDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        return repository.getAll(limit, offset)
                .chain(list -> Uni.join().all(
                        list.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList())
                ).andFailFast());
    }

    @Override
    public Uni<TaskTypeDTO> getDTOByIdentifier(String identifier) {
        return repository.findByIdentifier(identifier).chain(this::mapToDTO);
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<TaskType> getById(UUID uuid) {
        return repository.findById(uuid);
    }

    public Uni<TaskType> getByIdentifier(String uuid) {
        return repository.findByIdentifier(uuid);
    }

    @Override
    public Uni<TaskTypeDTO> getDTO(UUID uuid, IUser user, LanguageCode language) {
        return repository.findById(uuid).chain(this::mapToDTO);
    }

    private Uni<TaskTypeDTO> mapToDTO(TaskType doc) {
        return Uni.combine().all().unis(
                userRepository.getUserName(doc.getAuthor()),
                userRepository.getUserName(doc.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                TaskTypeDTO.builder()
                        .id(doc.getId())
                        .author(tuple.getItem1())
                        .regDate(doc.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(doc.getLastModifiedDate())
                        .identifier(doc.getIdentifier())
                        .localizedName(doc.getLocalizedName())
                        .build()
        );
    }

    @Override
    public Uni<TaskTypeDTO> upsert(String id, TaskTypeDTO dto, IUser user, LanguageCode code) {
        return null;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }

    @Deprecated
    public Uni<? extends Optional<TaskType>> findById(UUID taskType) {
        return null;
    }
}