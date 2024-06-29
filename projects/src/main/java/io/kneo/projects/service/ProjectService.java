package io.kneo.projects.service;

import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
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
    private final ProjectRepository repository;

    protected ProjectService() {
        super(null, null);
        this.repository = null;
    }

    @Inject
    public ProjectService(UserRepository userRepository, UserService userService, ProjectRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

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

    public Uni<ProjectDTO> get(UUID id, IUser user) {
        return get(id, user.getId(), false);
    }

    public Uni<ProjectDTO> get(UUID id, final long userID, boolean includeRLS) {
        Uni<Optional<Project>> projectUni = repository.findById(id, userID);

        Uni<List<RLSDTO>> rlsDtoListUni;

        if (includeRLS) {
            rlsDtoListUni = getRLSDTO(repository, ProjectNameResolver.create().getEntityNames(PROJECT), projectUni, id);
        } else {
            rlsDtoListUni = Uni.createFrom().optional(Optional.empty());
        }

        return projectUni.flatMap(projectOptional -> {
            if (projectOptional.isEmpty()) {
                return Uni.createFrom().failure(new DocumentHasNotFoundException(id));
            }

            Project project = projectOptional.get();

            return Uni.combine().all().unis(Uni.createFrom().item(project), rlsDtoListUni)
                    .combinedWith((proj, rls) -> ProjectDTO.builder()
                            .id(proj.getId())
                            .name(proj.getName())
                            .description(proj.getDescription())
                            .status(proj.getStatus())
                            .finishDate(proj.getFinishDate())
                            .manager(PlainUserDTO.builder()
                                    .id(proj.getManager())
                                    .name(userService.getUserName(proj.getManager()))
                                    .build())
                            .coder(PlainUserDTO.builder()
                                    .id(proj.getCoder())
                                    .name(userService.getUserName(proj.getCoder()))
                                    .build())
                            .tester(PlainUserDTO.builder()
                                    .id(proj.getTester())
                                    .name(userService.getUserName(proj.getTester()))
                                    .build())
                            .rls(rls)
                            .primaryLang(proj.getPrimaryLang())
                            .build());
        });
    }

    @Override
    public Uni<ProjectDTO> getDTO(String uuid, IUser user, LanguageCode code) {
        return get(UUID.fromString(uuid), user.getId(), true);
    }

    @Override
    public Uni<UUID> add(ProjectDTO dto, IUser user) {
        Project node = new Project.Builder()
                .setName(dto.getName())
                .build();
        repository.insert(node, AnonymousUser.ID);
        return Uni.createFrom().nullItem();
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

    private Project buildEntity(ProjectDTO dto, Optional<IUser> manager, Optional<IUser> coder, Optional<IUser> tester) {
        return new Project.Builder()
                .setName(dto.getName())
                .setManager(manager.orElseThrow(() -> new DataValidationException("Manager not found")).getId())
                .setCoder(coder.orElseThrow(() -> new DataValidationException("Coder not found")).getId())
                .setTester(tester.orElseThrow(() -> new DataValidationException("Tester not found")).getId())
                .setFinishDate(dto.getFinishDate())
                .setStatus(dto.getStatus())
               // .setPrimaryLang(dto.getPrimaryLang())
                .setDescription(dto.getDescription())
                .build();
    }



}
