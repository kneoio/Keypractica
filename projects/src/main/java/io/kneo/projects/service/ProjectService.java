package io.kneo.projects.service;

import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.core.service.exception.DataValidationException;
import io.kneo.officeframe.dto.PlainUserDTO;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.model.Project;
import io.kneo.projects.model.cnst.ProjectStatusType;
import io.kneo.projects.repository.ProjectRepository;
import io.kneo.projects.repository.table.ProjectNameResolver;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.projects.repository.table.ProjectNameResolver.PROJECT;

@ApplicationScoped
public class ProjectService extends AbstractService<Project, ProjectDTO> {
    @Inject
    private ProjectRepository repository;
    @Inject
    private UserService userService;

    public Uni<List<ProjectDTO>> getAll(final int limit, final int offset, final long userID) {
        Uni<List<Project>> uni = repository.getAll(limit, offset, userID);
        return uni
                .onItem().transform(projectList -> projectList.stream()
                        .map(this::map)
                        .collect(Collectors.toList()));
    }
    public Uni<Integer> getAllCount(final long userID) {
        return repository.getAllCount(userID);
    }

    public Uni<List<Project>> search(String keyword) {
        return repository.search(keyword);
    }

    public Uni<List<ProjectDTO>> searchByStatus(ProjectStatusType statusType) {
        Uni<List<Project>> uni = repository.searchByCondition(String.format("status = '%s'", statusType));
        return uni
                .onItem().transform(projectList -> projectList.stream()
                        .map(this::map)
                        .collect(Collectors.toList()));
    }

    public Uni<ProjectDTO> get(String uuid, final long userID) {
        return get(UUID.fromString(uuid), userID);
    }
    public Uni<ProjectDTO> get(UUID id, IUser user) {
        return get(id, user.getId(), false);
    }

    public Uni<ProjectDTO> getDTO(String uuid, IUser user) {
        return get(uuid, user.getId());
    }

    private ProjectDTO map(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .author(userRepository.getUserName(project.getAuthor()))
                .regDate(project.getRegDate())
                .lastModifier(userRepository.getUserName(project.getLastModifier()))
                .lastModifiedDate(project.getLastModifiedDate())
                .name(project.getName())
                .finishDate(project.getFinishDate())
                .status(project.getStatus())
                .manager(PlainUserDTO.builder()
                        .id(project.getManager())
                        .name(userService.getUserName(project.getManager()))
                        .build())
                .coder(PlainUserDTO.builder()
                        .id(project.getManager())
                        .name(userService.getUserName(project.getCoder()))
                        .build())
                .tester(PlainUserDTO.builder()
                        .id(project.getManager())
                        .name(userService.getUserName(project.getCoder()))
                        .build())
                .build();
    }

    @Override
    public Uni<UUID> add(ProjectDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> update(String id, ProjectDTO dto, IUser user) {
        Uni<Optional<IUser>> managerUni = userService.get(dto.getManager().getId());
        Uni<Optional<IUser>> coderUni = userService.get(dto.getCoder().getId());
        Uni<Optional<IUser>> testerUni = userService.get(dto.getTester().getId());
        return Uni.combine().all().unis(managerUni, coderUni, testerUni).combinedWith((manager, coder, tester) -> {
            Project doc = buildEntity(dto, manager, coder, tester);
            return repository.update(UUID.fromString(id), doc, user.getId());
        }).flatMap(uni -> uni);
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }
    public Uni<ProjectDTO> get(UUID id, final long userID) {
        return get(id, userID, true);
    }

    public Uni<ProjectDTO> get(UUID id, final long userID, boolean includeRLS) {
        Uni<Optional<Project>> projectUni = repository.findById(id, userID);

        Uni<List<RLSDTO>> rlsDtoListUni;

        if (includeRLS) {
            rlsDtoListUni = getRLSDTO(repository, ProjectNameResolver.create().getEntityNames(PROJECT), projectUni, id);
        } else {
            rlsDtoListUni = Uni.createFrom().optional(Optional.empty());
        }

        return Uni.combine().all().unis(projectUni, rlsDtoListUni).combinedWith((projectOptional, rls) -> {
            Project project = projectOptional.get();
            return ProjectDTO.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .status(project.getStatus())
                    .finishDate(project.getFinishDate())
                    .manager(PlainUserDTO.builder()
                            .id(project.getManager())
                            .name(userService.getUserName(project.getManager()))
                            .build())
                    .coder(PlainUserDTO.builder()
                            .id(project.getCoder())
                            .name(userService.getUserName(project.getCoder()))
                            .build())
                    .tester(PlainUserDTO.builder()
                            .id(project.getTester())
                            .name(userService.getUserName(project.getTester()))
                            .build())
                    .rls(rls)
                    .primaryLang(project.getPrimaryLang())
                    .build();
        });
    }

    public Uni<ProjectDTO> add(ProjectDTO dto) {
        Project node = new Project.Builder()
                .setName(dto.getName())
                .build();
                repository.insert(node, AnonymousUser.ID);
        return Uni.createFrom().nullItem();
    }

    private Project buildEntity(ProjectDTO dto, Optional<IUser> manager, Optional<IUser> coder, Optional<IUser> tester) {
        Project project = new Project.Builder()
                .setName(dto.getName())
                .setManager(manager.orElseThrow(() -> new DataValidationException("Manager not found")).getId())
                .setCoder(coder.orElseThrow(() -> new DataValidationException("Coder not found")).getId())
                .setTester(tester.orElseThrow(() -> new DataValidationException("Tester not found")).getId())
                .setFinishDate(dto.getFinishDate())
                .setStatus(dto.getStatus())
               // .setPrimaryLang(dto.getPrimaryLang())
                .setDescription(dto.getDescription())
                .build();
        return project;
    }



}
