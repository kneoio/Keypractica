package io.kneo.projects.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.repository.table.EntityData;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.dto.TaskTypeDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.service.EmployeeService;
import io.kneo.officeframe.service.LabelService;
import io.kneo.officeframe.service.TaskTypeService;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.dto.filter.TaskFilter;
import io.kneo.projects.model.Project;
import io.kneo.projects.model.Task;
import io.kneo.projects.repository.TaskRepository;
import io.kneo.projects.repository.table.ProjectNameResolver;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.projects.repository.table.ProjectNameResolver.TASK;

@ApplicationScoped
public class TaskService extends AbstractService<Task, TaskDTO> {
    private static final EntityData entityData = ProjectNameResolver.create().getEntityNames(TASK);
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
                                            return employeeService.getByUserId(doc.getAssignee())
                                                    .onFailure(DocumentHasNotFoundException.class).recoverWithNull()
                                                    .onItem().transform(assignee ->
                                                            TaskDTO.builder()
                                                                    .id(doc.getId())
                                                                    .author(userRepository.getUserName(doc.getAuthor()).await().atMost(TIMEOUT))
                                                                    .regDate(doc.getRegDate())
                                                                    .lastModifier(userRepository.getUserName(doc.getLastModifier()).await().atMost(TIMEOUT))
                                                                    .lastModifiedDate(doc.getLastModifiedDate())
                                                                    .targetDate(doc.getTargetDate())
                                                                    .priority(doc.getPriority())
                                                                    .status(doc.getStatus())
                                                                    //.assignee(assignee)
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
    public Uni<TaskDTO> upsert(String id, TaskDTO dto, IUser user, LanguageCode code) {
        assert projectService != null;
        assert employeeService != null;
        assert labelService != null;
        assert taskTypeService != null;

        Uni<Project> projectUni = projectService.getById(dto.getProject().getId(), user);
        Uni<Employee> assigneeUni = employeeService.getByUserId(dto.getAssignee().getUserId());
        Uni<TaskType> taskTypeUni = taskTypeService.getByIdentifier(dto.getTaskType().getIdentifier());

        List<UUID> labelIds = dto.getLabels().stream()
                .map(LabelDTO::getId)
                .collect(Collectors.toList());

        Task doc = new Task();
        doc.setStatus(dto.getStatus());
        doc.setBody(dto.getBody());
        doc.setCancellationComment(dto.getCancellationComment());
        doc.setPriority(dto.getPriority());
        doc.setTargetDate(dto.getTargetDate());
        doc.setTitle(dto.getTitle());

        assert repository != null;
        return Uni.combine().all().unis(projectUni, assigneeUni, taskTypeUni).asTuple().onItem().transformToUni(tuple -> {
            Project project = tuple.getItem1();
            Employee assignee = tuple.getItem2();
            TaskType taskType = tuple.getItem3();

            doc.setProject(project.getId());
            doc.setAssignee(assignee.getUserId());
            doc.setTaskType(taskType.getId());
            doc.setLabels(labelIds);

            Uni<Task> taskUni;
            if (id == null) {
                doc.setRegNumber(dto.getRegNumber());
                taskUni = repository.insert(doc, user);
            } else {
                taskUni = repository.update(UUID.fromString(id), doc, user);
            }

            return map(taskUni, user, LanguageCode.ENG);
        });
    }


    public Uni<Integer> delete(String id, IUser user) {
        UUID uuid = UUID.fromString(id);
        assert repository != null;
        return repository.delete(uuid, user);
    }

    private Uni<TaskDTO> map(Uni<Task> taskUni, IUser user, LanguageCode code) {
        assert projectService != null;
        assert taskTypeService != null;
        assert employeeService != null;
        assert labelService != null;

        return taskUni.onItem().transformToUni(task -> {
            Uni<ProjectDTO> projectUni = projectService.getDTO(task.getProject(), user, code);
            Uni<TaskTypeDTO> taskTypeUni = taskTypeService.getDTO(task.getTaskType(), user, code);
            Uni<EmployeeDTO> assigneeUni = employeeService.getDTOByUserId(task.getAssignee(), code)
                    .onFailure(DocumentHasNotFoundException.class).recoverWithItem(() -> null);
            Uni<List<LabelDTO>> labelsUni = labelService.getLabels(task.getId(), entityData.getLabelsName());

            return Uni.combine().all().unis(projectUni, assigneeUni, taskTypeUni, labelsUni).asTuple().onItem().transform(tuple -> {
                ProjectDTO project = tuple.getItem1();
                EmployeeDTO assignee = tuple.getItem2();
                TaskTypeDTO taskType = tuple.getItem3();
                List<LabelDTO> labels = tuple.getItem4();

                return TaskDTO.builder()
                        .id(task.getId())
                        .author(userRepository.getUserName(task.getAuthor()).await().atMost(TIMEOUT))
                        .regDate(task.getRegDate())
                        .title(task.getTitle())
                        .lastModifier(userRepository.getUserName(task.getLastModifier()).await().atMost(TIMEOUT))
                        .lastModifiedDate(task.getLastModifiedDate())
                        .regNumber(task.getRegNumber())
                        .startDate(task.getStartDate())
                        .targetDate(task.getTargetDate())
                        .body(task.getBody())
                        .assignee(assignee)
                        .taskType(taskType)
                        .project(project)
                        .startDate(task.getStartDate())
                        .targetDate(task.getTargetDate())
                        .status(task.getStatus())
                        .priority(task.getPriority())
                        .labels(labels)
                        .build();
            });
        });
    }

}
