package io.kneo.projects.service;

import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.model.user.SuperUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.core.service.exception.ServiceException;
import io.kneo.core.util.NumberUtil;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.model.Label;
import io.kneo.officeframe.model.TaskType;
import io.kneo.officeframe.service.LabelService;
import io.kneo.officeframe.service.TaskTypeService;
import io.kneo.projects.dto.AssigneeDTO;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.dto.TaskDTO;
import io.kneo.projects.dto.TaskTypeDTO;
import io.kneo.projects.model.Task;
import io.kneo.projects.repository.TaskRepository;
import io.kneo.projects.repository.table.ProjectNameResolver;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.projects.repository.table.ProjectNameResolver.PROJECT;
import static io.kneo.projects.repository.table.ProjectNameResolver.TASK;

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
    private TaskTypeService taskTypeService;

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
                                        .startDate(LocalDate.from(task.getStartDate()))
                                        .targetDate(Optional.ofNullable(task.getTargetDate()).map((LocalDate::from)).orElse(null))
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
        Uni<Optional<Task>> taskUni = repository.findById(id, userID);

        Uni<ProjectDTO> projectUni = taskUni.onItem().transformToUni(item ->
                projectService.get(item.get().getProject(), userID)
        );

        Uni<Optional<TaskType>> taskTypeUni = taskUni.onItem().transformToUni(item ->
                taskTypeService.findById(item.get().getTaskType())
        );

        Uni<List<RLSDTO>> rlsDtoListUni = getRLSDTO(repository,ProjectNameResolver.create().getEntityNames(TASK), taskUni, id);

   /*     List<Uni<Optional<Label>>> labelUnis = taskUni.onItem().transformToUni(item ->
                item.get().getLabels().stream().map(v -> labelService.get(v))

        )*/

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
                            .assignee(getAssigneeDTO(userService.findById(task.getAssignee()), task))
                            .taskType(TaskTypeDTO.builder()
                                    .identifier(taskType.orElseThrow().getIdentifier())
                                    .localizedName(taskType.orElseThrow().getLocName(LanguageCode.ENG))
                                    .build())
                            .project(project)
                            .startDate(LocalDate.from(task.getStartDate()))
                            .targetDate(Optional.ofNullable(task.getTargetDate()).map(LocalDate::from).orElse(null))
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
        List<LabelDTO> labelDTOs = dto.getLabels();
        List<Uni<Optional<Label>>> labelUnis = labelDTOs.stream()
                .map(v ->
                        labelService.findByIdentifier(v.getIdentifier())
                                .onItem()
                                .transform(item -> item)
                )
                .collect(Collectors.toList());


        Uni<List<Optional<Label>>> combinedLabelUnis;
        if (labelUnis.isEmpty()) {
            combinedLabelUnis = Uni.createFrom().item(Collections.emptyList());
        } else {
            combinedLabelUnis = Uni.combine().all().unis(labelUnis).combinedWith(list -> (List<Optional<Label>>) list);

        }

        Uni<Optional<IUser>> assigneeUni = userService.get(dto.getAssignee().getId());
        Uni<Optional<TaskType>> taskTypeUni = taskTypeService.findByIdentifier(dto.getTaskType().getIdentifier());
        Uni<ProjectDTO> projectUni = projectService.get(dto.getProject().getId(), user);
        Uni<Optional<Task>> taskUni = repository.findById(dto.getId(), user.getId());

        return Uni.combine().all().unis(assigneeUni, taskTypeUni, projectUni, taskUni, combinedLabelUnis).combinedWith((assignee, taskType, project, taskOpt, labels) -> {
            Task node = new Task.Builder()
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
                    .setStartDate(dto.getStartDate().atStartOfDay(ZoneId.systemDefault()))
                    .build();
            taskOpt.ifPresent(task -> node.setParent(task.getParent()));
            if (dto.getTargetDate() != null) {
                node.setTargetDate(dto.getTargetDate().atStartOfDay(ZoneId.systemDefault()));
            }
            return repository.insert(node, user.getId());
        }).flatMap(uni -> uni);
    }


    public Uni<Integer> update(TaskDTO dto, IUser user) throws DocumentModificationAccessException {
        Task doc = new Task.Builder()
                .setId(dto.getId())
                //  .setAssignee(Long.valueOf(dto.getAssignee()))
                .setBody(dto.getBody())
                .build();
        return repository.update(doc, user.getId());
    }
}
