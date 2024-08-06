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
        Uni<List<TaskType>> taskUni = repository.getAll(limit, offset);
        return taskUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(e ->
                                TaskTypeDTO.builder()
                                        .id(e.getId())
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .identifier(e.getIdentifier())
                                        .localizedName(e.getLocalizedName())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<TaskTypeDTO> getByIdentifier(String identifier) {
        return null;
        //TODO shall be used in controller
    }

    public Uni<Optional<TaskType>> findByIdentifier(String  identifier) {
        return repository.findByIdentifier(identifier);
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<TaskType> getById(UUID uuid) {
        return repository.findById(uuid);
    }

    @Override
    public Uni<TaskTypeDTO> getDTO(UUID uuid, IUser user, LanguageCode language) {
        Uni<TaskType> uni = repository.findById(uuid);
        return uni.onItem().transform(doc -> {
            return TaskTypeDTO.builder()
                    .author(userRepository.getUserName(doc.getAuthor()))
                    .regDate(doc.getRegDate())
                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .identifier(doc.getIdentifier())
                    .localizedName(doc.getLocalizedName())
                    .build();

        });
    }

    @Override
    public Uni<TaskTypeDTO> upsert(UUID id, TaskTypeDTO dto, IUser user, LanguageCode code) {
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
