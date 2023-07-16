package com.semantyca.projects.service;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.repository.UserRepository;
import com.semantyca.officeframe.model.TaskType;
import com.semantyca.officeframe.repository.TaskTypeRepository;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.dto.TaskDTO;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.model.Task;
import com.semantyca.projects.repository.ProjectRepository;
import com.semantyca.projects.repository.TaskRepository;
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
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger("TaskService");
    @Inject
    private TaskRepository repository;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private TaskTypeRepository taskTypeRepository;

    public Uni<List<TaskDTO>> getAll(final int limit, final int offset, final long userID) {
        Uni<List<Task>> taskUni = repository.getAll(limit, offset, userID);

     /*   Uni<Optional<TaskType>> taskTypeUni = taskUni.onItem().transformToUni(item ->
                taskTypeRepository.findById(item.get().getTaskType())
        );*/

        return taskUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(task ->
                            new TaskDTO(task.getId(), task.getRegNumber(), task.getBody(), null, null , null, null, task.getStartDate(), task.getTargetDate(), task.getStatus(), task.getPriority(), null))
                        .collect(Collectors.toList()));
    }

    public Uni<TaskDTO> get(String uuid) {
        UUID id = UUID.fromString(uuid);
        Uni<Optional<Task>> taskUni = repository.findById(2L, id);
        Uni<Optional<IUser>> asigneeUni = taskUni.onItem().transformToUni(item ->
                userRepository.findById(item.get().getAssignee())
        );

        Uni<Optional<Project>> projectUni = taskUni.onItem().transformToUni(item ->
                projectRepository.findById(item.get().getProject(), 2L)
        );


        Uni<Optional<TaskType>> taskTypeUni = taskUni.onItem().transformToUni(item ->
                taskTypeRepository.findById(item.get().getTaskType())
        );

        Uni<List<RLS>> rlsEntires = taskUni.onItem().transformToUni(item ->
                repository.getAllReaders(id)
        );

        return Uni.combine().all().unis(taskUni, asigneeUni, projectUni, taskTypeUni, rlsEntires).combinedWith((taskOpt, userOptional, projectOpt, taskType, rlsList) -> {
                    Task p = taskOpt.get();
                    return new TaskDTO(p.getId(),p.getRegNumber(), p.getBody(), userOptional.get().getUserName(), taskType.get().getLocName(), projectOpt.orElse(new Project()), null, p.getStartDate(), p.getTargetDate(), p.getStatus(), p.getPriority(), rlsList);
                }
        );

    }

    public String add(ProjectDTO dto)  {
        Project node = new Project.Builder()
                .setName(dto.name())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.code())
                .build();
        return repository.update(user);
    }
}
