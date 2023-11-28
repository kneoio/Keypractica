package io.kneo.projects.service;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.model.Language;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.service.AbstractService;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.repository.TaskTypeRepository;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.model.Task;
import io.kneo.projects.repository.TaskRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TaskService extends AbstractService<Task, TaskDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger("TaskService");
    @Inject
    private TaskRepository repository;
    @Inject
    private ProjectService projectService;
    @Inject
    private TaskTypeRepository taskTypeRepository;

    public Uni<List<TaskDTO>> getAll(final int limit, final int offset, final long userID) {
        Uni<List<Task>> taskUni = repository.getAll(limit, offset, userID);
        return taskUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(task ->
                                TaskDTO.builder()
                                        .id(task.getId())
                                        .author(userRepository.getUserName(task.getAuthor()))
                                        .regDate(task.getRegDate())
                                        .lastModifier(userRepository.getUserName(task.getLastModifier()))
                                        .lastModifiedDate(task.getLastModifiedDate())
                                        .regNumber(task.getRegNumber())
                                        .body(task.getBody())
                                        .startDate(task.getStartDate())
                                        .targetDate(task.getTargetDate())
                                        .status(task.getStatus())
                                        .priority(task.getPriority())
                                        .build())
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(final long userID) {
        return repository.getAllCount(userID);
    }

    public Uni<TaskDTO> get(String uuid) {
        return get(uuid, SuperUser.ID);
    }
    public Uni<TaskDTO> get(String uuid, final long userID) {
        UUID id = UUID.fromString(uuid);
        Uni<Task> taskUni = repository.findById(userID, id);

        Uni<ProjectDTO> projectUni = taskUni.onItem().transformToUni(item ->
                projectService.get(item.getProject(), userID)
        );

        Uni<Optional<TaskType>> taskTypeUni = taskUni.onItem().transformToUni(item ->
                taskTypeRepository.findById(item.getTaskType())
        );

        Uni<List<RLSDTO>> rlsDtoListUni = getRLSDTO(repository, taskUni, id);

        return Uni.combine().all().unis(taskUni, projectUni, taskTypeUni, rlsDtoListUni).combinedWith((taskOpt, project, taskType, rls) -> {
                    Task task = taskOpt;
                    return TaskDTO.builder()
                            .id(task.getId())
                            .author(userRepository.getUserName(task.getAuthor()))
                            .regDate(task.getRegDate())
                            .lastModifier(userRepository.getUserName(task.getLastModifier()))
                            .lastModifiedDate(task.getLastModifiedDate())
                            .regNumber(task.getRegNumber())
                            .body(task.getBody())
                            .assignee(userRepository.getUserName(task.getAssignee()))
                            .taskType(taskType.orElseThrow().getLocalizedName())
                            .project(project)
                            .startDate(task.getStartDate())
                            .targetDate(task.getTargetDate())
                            .status(task.getStatus())
                            .priority(task.getPriority())
                            .rls(rls).build();
                }
        );

    }

    public Uni<UUID> add(TaskDTO dto) {
        Task node = new Task.Builder()
                .setId(dto.getId())
                .setAssignee(Long.valueOf(dto.getAssignee()))
                .setBody(dto.getBody())
                .build();
        return repository.insert(node, AnonymousUser.ID);
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.getCode())
                .build();
        return repository.update(user);
    }
}
