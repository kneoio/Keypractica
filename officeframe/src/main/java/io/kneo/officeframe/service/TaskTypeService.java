package io.kneo.officeframe.service;

import io.kneo.core.model.user.IUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.officeframe.dto.LabelDTO;
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
    @Inject
    private TaskTypeRepository repository;

    public Uni<List<TaskTypeDTO>> getAll(final int limit, final int offset) {
        Uni<List<TaskType>> taskUni = repository.getAll(limit, offset);
        return taskUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(e ->
                                TaskTypeDTO.builder()
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .identifier(e.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Optional<TaskTypeDTO>> getByIdentifier(String identifier) {
        return null;
        //TODO shall be used in controller
    }

    public Uni<Optional<TaskType>> findById(UUID uuid) {
        return repository.findById(uuid);
    }
    public Uni<Optional<TaskType>> findByIdentifier(String  identifier) {
        return repository.findByIdentifier(identifier);
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<TaskTypeDTO> getDTO(String uuid, IUser user) {
        return get(UUID.fromString(uuid));
    }

    @Override
    public Uni<UUID> add(TaskTypeDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> update(String id, TaskTypeDTO dto, IUser user) {
        return null;
    }

    public Uni<TaskTypeDTO> get(UUID uuid) {
       return null;
    }

    public Uni<Object> add(LabelDTO dto) {
        return null;
    }

    public IUser update(LabelDTO dto) {
        return null;
    }
}
