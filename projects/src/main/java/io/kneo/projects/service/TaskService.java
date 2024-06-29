package io.kneo.projects.service;

import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.util.DateUtil;
import io.kneo.core.util.NumberUtil;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.model.Label;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.service.LabelService;
import io.kneo.officeframe.service.TaskTypeService;
import io.kneo.projects.dto.*;
import io.kneo.projects.model.Task;
import io.kneo.projects.model.cnst.TaskStatus;
import io.kneo.projects.repository.TaskRepository;
import io.kneo.projects.repository.table.ProjectNameResolver;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.projects.repository.table.ProjectNameResolver.TASK;

@ApplicationScoped
public class TaskService extends AbstractService<Task, TaskDTO> {
    private final TaskRepository repository;
    private final LabelService labelService;
    private final ProjectService projectService;
    private final TaskTypeService taskTypeService;

    protected TaskService() {
        super(null, null);
        this.repository = null;
        this.labelService = null;
        this.projectService = null;
        this.taskTypeService = null;
    }

    @Inject
    public TaskService(UserRepository userRepository,
                       UserService userService,
                       TaskRepository repository,
                       LabelService labelService,
                       ProjectService projectService,
                       TaskTypeService taskTypeService) {
        super(userRepository, userService);
        this.repository = repository;
        this.labelService = labelService;
        this.projectService = projectService;
        this.taskTypeService = taskTypeService;
    }

    public Uni<List<TaskDTO>> getAll(final int limit, final int offset, final long userID) {
        Uni<List<Task>> taskUni = repository.getAll(limit, offset, userID);
        return taskUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(this::map)
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(final long userID) {
        return repository.getAllCount(userID);
    }

    public Uni<List<TaskDTO>> searchByStatus(TaskStatus statusType) {
        Uni<List<Task>> uni = repository.searchByCondition(String.format("status = '%s'", statusType.getCode()));
        return uni
                .onItem().transform(projectList -> projectList.stream()
                        .map(this::map)
                        .collect(Collectors.toList()));
    }

    private TaskDTO map(Task doc) {
        return TaskDTO.builder()
                .id(doc.getId())
                .author(userRepository.getUserName(doc.getAuthor()))
                .regDate(doc.getRegDate())
                .title(doc.getTitle())
                .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                .lastModifiedDate(doc.getLastModifiedDate())
                .regNumber(doc.getRegNumber())
                .body(doc.getBody())
                .startDate(LocalDate.from(doc.getStartDate()))
                .targetDate(Optional.ofNullable(doc.getTargetDate()).map((LocalDate::from)).orElse(null))
                .status(TaskStatus.getType(doc.getStatus()))
                .priority(doc.getPriority())
                .build();
    }

    @Override
    public Uni<TaskDTO> getDTO(String uuid, IUser user, LanguageCode code) {
        return get(uuid, user);
    }

    public Uni<TaskDTO> get(String uuid, final IUser userID) {
        UUID id = UUID.fromString(uuid);
        Uni<Optional<Task>> taskUni = repository.findById(id, userID.getId());

        Uni<ProjectDTO> projectUni = taskUni.onItem().transformToUni(item ->
                projectService.get(item.get().getProject(), userID)
        );

        Uni<Optional<TaskType>> taskTypeUni = taskUni.onItem().transformToUni(item ->
                taskTypeService.findById(item.get().getTaskType())        );

        Uni<List<LabelDTO>> labelsUni = labelService.getLabels(id, ProjectNameResolver.create().getEntityNames(TASK).getLabelsName());

        Uni<List<RLSDTO>> rlsDtoListUni = getRLSDTO(repository,ProjectNameResolver.create().getEntityNames(TASK), taskUni, id);


        return Uni.combine().all().unis(taskUni, projectUni, taskTypeUni, labelsUni, rlsDtoListUni).combinedWith((taskOpt, project, taskType, labels, rls) -> {
                    Task task = taskOpt.orElseThrow();
                    return TaskDTO.builder()
                            .id(task.getId())
                            .author(userRepository.getUserName(task.getAuthor()))
                            .regDate(task.getRegDate())
                            .title(task.getTitle())
                            .lastModifier(userRepository.getUserName(task.getLastModifier()))
                            .lastModifiedDate(task.getLastModifiedDate())
                            .regNumber(task.getRegNumber())
                            .body(task.getBody())
                            .assignee(getAssigneeDTO(userService.findById(task.getAssignee()), task))
                            .taskType(TaskTypeDTO.builder()
                                    .identifier(taskType.orElseThrow().getIdentifier())
                                    .localizedName(taskType.orElseThrow().getLocalizedName(LanguageCode.ENG))
                                    .build())
                            .project(project)
                            .startDate(LocalDate.from(task.getStartDate()))
                            .targetDate(Optional.ofNullable(task.getTargetDate()).map(LocalDate::from).orElse(null))
                            .status(TaskStatus.getType(task.getStatus()))
                            .priority(task.getPriority())
                            .labels(labels)
                            .rls(rls).build();
                }
        );

    }

    public Uni<TaskTemplateDTO> getTemplate(IUser user) {
        TaskTemplateDTO template = TaskTemplateDTO.builder()
                .targetDate(LocalDate.now().plusMonths(1))
                .build();
        return Uni.createFrom().item(template);
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
        Uni<List<Optional<Label>>> combinedLabelUnis = getLabelsUni( dto.getLabels());
        Uni<Optional<IUser>> assigneeUni = userService.get(dto.getAssignee().getId());
        Uni<Optional<TaskType>> taskTypeUni = taskTypeService.findByIdentifier(dto.getTaskType().getIdentifier());
        Uni<ProjectDTO> projectUni = projectService.get(dto.getProject().getId(), user);
        Uni<Optional<Task>> taskUni = repository.findById(dto.getId(), user.getId());
        return Uni.combine().all().unis(assigneeUni, taskTypeUni, projectUni, taskUni, combinedLabelUnis).combinedWith((assignee, taskType, project, taskOpt, labels) -> {
            Task doc = buildEntity(dto, assignee, labels,  taskType, project, taskOpt);
            return repository.insert(doc, user.getId());
        }).flatMap(uni -> uni);
    }

    public Uni<Integer> update(String id, TaskDTO dto, IUser user) {
        Uni<List<Optional<Label>>> combinedLabelUnis = getLabelsUni( dto.getLabels());
        Uni<Optional<IUser>> assigneeUni = userService.get(dto.getAssignee().getId());
        Uni<Optional<TaskType>> taskTypeUni = taskTypeService.findByIdentifier(dto.getTaskType().getIdentifier());
        Uni<ProjectDTO> projectUni = projectService.get(dto.getProject().getId(), user);
        Uni<Optional<Task>> taskUni = repository.findById(dto.getId(), user.getId());
        return Uni.combine().all().unis(assigneeUni, taskTypeUni, projectUni, taskUni, combinedLabelUnis).combinedWith((assignee, taskType, project, taskOpt, labels) -> {
            Task doc = buildEntity(dto, assignee, labels,  taskType, project, taskOpt);
            return repository.update(UUID.fromString(id), doc, user.getId());
        }).flatMap(uni -> uni);
    }

    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id), user.getId());
    }

    private Task buildEntity(TaskDTO dto, Optional<IUser> assignee, List<Optional<Label>> labels, Optional<TaskType> taskType, ProjectDTO project, Optional<Task> taskOpt) {
        Task doc = new Task.Builder()
                .setRegNumber(String.valueOf(NumberUtil.getRandomNumber(100000, 999999)))
                .setBody(dto.getBody())
                .setAssignee(assignee.orElseThrow().getId())
                .setPriority(dto.getPriority())
                .setCancellationComment(dto.getCancellationComment())
                .setTitle(dto.getTitle())
                .setLabels(labels.stream()
                        .filter(Optional::isPresent)
                        .map(o -> o.get().getId())
                        .collect(Collectors.toList()))
                .setTaskType(taskType.orElseThrow(() -> new DataValidationException("Task type is not correct")).getId())
                .setProject(project.getId())
                .setStartDate(DateUtil.getStartOfDayOrNow(dto.getStartDate()))
                .build();
        taskOpt.ifPresent(task -> doc.setParent(task.getParent()));
        if (dto.getTargetDate() != null) {
            doc.setTargetDate(dto.getTargetDate().atStartOfDay(ZoneId.systemDefault()));
        }
        return doc;
    }

    private Uni<List<Optional<Label>>> getLabelsUni(List<LabelDTO> labelDTOs) {
        List<Uni<Optional<Label>>> labelUnis = labelDTOs.stream()
                .map(v ->
                        labelService.findByIdentifier(v.getIdentifier())
                                .onItem()
                                .transform(item -> item)
                )
                .collect(Collectors.toList());

        Uni<List<Optional<Label>>> combinedLabelUnis;
        if (labelUnis.isEmpty()) {
            return Uni.createFrom().item(Collections.emptyList());
        } else {
            return Uni.combine().all().unis(labelUnis).combinedWith(list -> (List<Optional<Label>>) list);
        }
    }


}
