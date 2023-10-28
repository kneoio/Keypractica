package io.kneo.projects.service;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.SuperUser;
import com.semantyca.core.service.AbstractService;
import com.semantyca.officeframe.model.TaskType;
import com.semantyca.officeframe.repository.TaskTypeRepository;
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
        Uni<Optional<Task>> taskUni = repository.findById(userID, id);

        Uni<ProjectDTO> projectUni = taskUni.onItem().transformToUni(item ->
                projectService.get(item.orElseThrow().getProject(), userID)
        );

        Uni<Optional<TaskType>> taskTypeUni = taskUni.onItem().transformToUni(item ->
                taskTypeRepository.findById(item.orElseThrow().getTaskType())
        );

        Uni<List<RLSDTO>> rlsDtoListUni = getRLSDTO(repository, taskUni, id);

        return Uni.combine().all().unis(taskUni, projectUni, taskTypeUni, rlsDtoListUni).combinedWith((taskOpt, project, taskType, rls) -> {
                    Task task = taskOpt.orElseThrow();
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

    public String add(TaskDTO dto) {
        Task node = new Task.Builder()
                .setBody(dto.getBody())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.getCode())
                .build();
        return repository.update(user);
    }
}
