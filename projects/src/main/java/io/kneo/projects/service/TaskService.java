package io.kneo.projects.service;

import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.core.util.NumberUtil;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.repository.TaskTypeRepository;
import io.kneo.officeframe.service.LabelService;
import io.kneo.projects.dto.AssigneeDTO;
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
    private UserService userService;
    @Inject
    private LabelService labelService;
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
                            .assignee(getAssigneeDTO(userService.findById(task.getAssignee()), task))
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

    private static AssigneeDTO getAssigneeDTO(Optional<IUser> assigneeOptional, Task task) {
        AssigneeDTO assigneeDTO = new AssigneeDTO();
        if (assigneeOptional.isPresent()) {
            IUser assignee = assigneeOptional.get();
            assigneeDTO.setFullName(assignee.getUserName());
            assigneeDTO.setId(assignee.getId());
            assigneeDTO.setAvailable(assignee.isActive());
        } else {
            assigneeDTO.setId(task.getAssignee());
            assigneeDTO.setAvailable(false);
        }
        return assigneeDTO;
    }

    public Uni<UUID> add(TaskDTO dto, IUser user) {
        Optional<IUser> assignee = userService.findById(dto.getAssignee().getId());
      //  dto.getLabels().stream().map(v -> labelService.get(v.getId());
      //  Optional<IUser> assignee = labelService.findById(dto.getLabels().getId());
        Task node = new Task.Builder()
                .setRegNumber(String.valueOf(NumberUtil.getRandomNumber(100000, 999999)))
                .setBody(dto.getBody())
                .setAssignee(assignee.orElseThrow().getId())
                .setPriority(dto.getPriority())
                .setTargetDate(dto.getTargetDate())
                .setCancellationComment(dto.getCancellationComment())
                .setTitle(dto.getTitle())
                //.setTags()
                //.setTaskType(dto.getTaskType())
                //.setProject(dto.getProject())
                //.setParent(dto.getParent())
                .build();
        return repository.insert(node, user.getId());
    }

    public Task update(TaskDTO dto) {
        Task doc = new Task.Builder()
                .setId(dto.getId())
              //  .setAssignee(Long.valueOf(dto.getAssignee()))
                .setBody(dto.getBody())
                .build();
        return repository.update(doc);
    }
}
