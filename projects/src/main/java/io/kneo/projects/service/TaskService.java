package io.kneo.projects.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.dto.TaskTypeDTO;
import io.kneo.officeframe.service.EmployeeService;
import io.kneo.officeframe.service.LabelService;
import io.kneo.officeframe.service.TaskTypeService;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.dto.filter.TaskFilter;
import io.kneo.projects.model.Task;
import io.kneo.projects.repository.TaskRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.projects.repository.table.ProjectNameResolver.TASK;

@ApplicationScoped
public class TaskService extends AbstractService<Task, TaskDTO> {
    private final TaskRepository repository;
    private final EmployeeService employeeService;
    private final LabelService labelService;
    private final ProjectService projectService;
    private final TaskTypeService taskTypeService;

    protected TaskService() {
        super(null, null);
        this.repository = null;
        this.employeeService = null;
        this.labelService = null;
        this.projectService = null;
        this.taskTypeService = null;
    }

    @Inject
    public TaskService(UserRepository userRepository, UserService userService, TaskRepository repository, EmployeeService employeeService, LabelService labelService, ProjectService projectService, TaskTypeService taskTypeService) {
        super(userRepository, userService);
        this.repository = repository;
        this.employeeService = employeeService;
        this.labelService = labelService;
        this.projectService = projectService;
        this.taskTypeService = taskTypeService;
    }

    public Uni<List<TaskDTO>> getAll(final int limit, final int offset, final IUser user, TaskFilter filters) {
        assert repository != null;
        Uni<List<Task>> taskUni = repository.getAll(limit, offset, user.getId());
        return taskUni
                .onItem().transformToUni(taskList ->
                        Uni.combine().all().unis(
                                taskList.stream()
                                        .map(doc -> {
                                            assert employeeService != null;
                                            return employeeService.getById(doc.getAssignee())
                                                    .onFailure(DocumentHasNotFoundException.class).recoverWithNull()
                                                    .onItem().transform(assignee ->
                                                            TaskDTO.builder()
                                                                    .id(doc.getId())
                                                                    .author(userRepository.getUserName(doc.getAuthor()))
                                                                    .regDate(doc.getRegDate())
                                                                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                                                                    .lastModifiedDate(doc.getLastModifiedDate())
                                                                    .targetDate(doc.getTargetDate())
                                                                    .priority(doc.getPriority())
                                                                    .status(doc.getStatus())
                                                                    .assignee(assignee)
                                                                    .build()
                                                    );
                                        }).collect(Collectors.toList())
                        ).with(list -> list.stream()
                                .map(item -> (TaskDTO) item)
                                .collect(Collectors.toList()))
                );
    }

    public Uni<Integer> getAllCount(final IUser user, TaskFilter filters) {
        assert repository != null;
        return repository.getAllCount(user.getId());
    }

    @Override
    public Uni<TaskDTO> getDTO(UUID uuid, IUser user, LanguageCode code) {
        assert repository != null;
        Uni<Task> taskUni = repository.findById(uuid, user.getId());
        return map(taskUni, user, code);
    }

    @Override
    public Uni<TaskDTO> upsert(UUID id, TaskDTO dto, IUser user, LanguageCode code) {
        UUID uuid;
        Uni<Task> taskUni;
        assert repository != null;
        if (id == null) {
            taskUni = repository.insert(buildEntity(dto, user), user);
        } else {
            taskUni = repository.update(id, buildEntity(dto, user), user);
        }

        return map(taskUni, user, LanguageCode.ENG);
    }


    public Uni<Integer> delete(String id, IUser user) {
        UUID uuid = UUID.fromString(id);
        return repository.delete(uuid, user);
    }

    private Uni<TaskDTO> map(Uni<Task> taskUni, IUser user, LanguageCode code) {
        return taskUni.onItem().transformToUni(task -> {
            Uni<ProjectDTO> projectUni = projectService.getDTO(task.getProject(), user, code);
            Uni<TaskTypeDTO> taskTypeUni = taskTypeService.getDTO(task.getTaskType(), user, code);
            Uni<EmployeeDTO> assigneeUni = employeeService.getById(task.getAssignee());
            Uni<List<LabelDTO>> labelsUni = labelService.getLabels(task.getId(), TASK);

            return Uni.combine().all().unis(projectUni, assigneeUni, taskTypeUni, labelsUni).asTuple().onItem().transform(tuple -> {
                ProjectDTO project = tuple.getItem1();
                EmployeeDTO assignee = tuple.getItem2();
                TaskTypeDTO taskType = tuple.getItem3();
                List<LabelDTO> labels = tuple.getItem4();

                return TaskDTO.builder()
                        .id(task.getId())
                        .author(userRepository.getUserName(task.getAuthor()))
                        .regDate(task.getRegDate())
                        .title(task.getTitle())
                        .lastModifier(userRepository.getUserName(task.getLastModifier()))
                        .lastModifiedDate(task.getLastModifiedDate())
                        .regNumber(task.getRegNumber())
                        .body(task.getBody())
                        .assignee(assignee)
                        .taskType(taskType)
                        .project(project)
                        .startDate(LocalDate.from(task.getStartDate()))
                        .targetDate(task.getTargetDate())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .labels(labels)
                        .build();
            });
        });
    }

    private Task buildEntity(TaskDTO dto, IUser userId) {
       /* return new Task.Builder()
                .title(dto.getTitle())
                .body(dto.getBody())
                .taskType(dto.getTaskType().getIdentifier())
                .assignee(dto.getAssignee().getIdentifier())
                .project(UUID.fromString(dto.getProject().getId()))
                .status(dto.getStatus())
                .priority(dto.getPriority())
                .startDate(dto.getStartDate())
                .targetDate(dto.getTargetDate())
                .author(userId)
                .lastModifier(userId)
                .build();*/
        return null;
    }
}
