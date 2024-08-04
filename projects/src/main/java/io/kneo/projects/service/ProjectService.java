package io.kneo.projects.service;

import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.model.Project;
import io.kneo.projects.model.cnst.ProjectStatusType;
import io.kneo.projects.repository.ProjectRepository;
import io.kneo.projects.repository.table.ProjectNameResolver;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.kneo.projects.repository.table.ProjectNameResolver.PROJECT;

@ApplicationScoped
public class ProjectService extends AbstractService<Project, ProjectDTO> {
    private final ProjectRepository repository;

    Validator validator;

    protected ProjectService() {
        super(null, null);
        this.repository = null;
    }

    @Inject
    public ProjectService(UserRepository userRepository, UserService userService,  Validator validator, ProjectRepository repository) {
        super(userRepository, userService);
        this.validator = validator;
        this.repository = repository;
    }

    public Uni<List<ProjectDTO>> getAll(final int limit, final int offset, final long userID) {
        Uni<List<Project>> uni = repository.getAll(limit, offset, userID);
        return uni
                .onItem().transform(projectList -> projectList.stream()
                        .map(this::plainMap)
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
                        .map(this::plainMap)
                        .collect(Collectors.toList()));
    }

    public Uni<ProjectDTO> getById(UUID id, IUser user) {
        return getById(id, user.getId(), false);
    }

    public Uni<ProjectDTO> getById(UUID id, final long userID, boolean includeRLS) {
        assert repository != null;
        Uni<Project> projectUni = repository.findById(id, userID);

        Uni<List<RLSDTO>> rlsDtoListUni;

        if (includeRLS) {
            rlsDtoListUni = getRLSDTO(repository, ProjectNameResolver.create().getEntityNames(PROJECT), projectUni, id);
        } else {
            rlsDtoListUni = Uni.createFrom().item(Collections.emptyList());
        }

        return projectUni.flatMap(doc -> {
            return rlsDtoListUni.map(rlsList -> ProjectDTO.builder()
                    .id(doc.getId())
                    .author(userRepository.getUserName(doc.getAuthor()))
                    .regDate(doc.getRegDate())
                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .name(doc.getName())
                    .description(doc.getDescription())
                    .status(doc.getStatus())
                    .finishDate(doc.getFinishDate())
                    .manager(UserDTO.builder()
                            // .identifier(doc.getManager())
                            .name(userService.getName(doc.getManager()))
                            .build())
                    .coder(UserDTO.builder()
                            // .identifier(doc.getCoder())
                            .name(userService.getName(doc.getCoder()))
                            .build())
                    .tester(UserDTO.builder()
                            // .identifier(doc.getTester())
                            .name(userService.getName(doc.getTester()))
                            .build())
                    .rls(rlsList)
                    .primaryLang(doc.getPrimaryLang())
                    .build());
        });
    }

    @Override
    public Uni<ProjectDTO> getDTO(String uuid, IUser user, LanguageCode code) {
        return getById(UUID.fromString(uuid), user.getId(), true);
    }

    @Override
    public Uni<ProjectDTO> upsert(String id, ProjectDTO dto, IUser user) {
        if (id == null) {
            assert repository != null;
            return repository.insert(buildEntity(dto), AnonymousUser.ID)
                    .onItem().transformToUni(project -> map(project));
        } else {
            UUID uuid = UUID.fromString(id);
            assert repository != null;
            return repository.update(uuid, buildEntity(dto), user.getId())
                    .onItem().transformToUni(this::map);
        }
    }


    private Uni<ProjectDTO> map(Project project) {
        Uni<String> managerNameUni = userService.getUserName(project.getManager());
        Uni<String> coderNameUni = userService.getUserName(project.getCoder());
        Uni<String> testerNameUni = userService.getUserName(project.getTester());

        return Uni.combine().all().unis(managerNameUni, coderNameUni, testerNameUni)
                .with((managerName, coderName, testerName) ->
                        ProjectDTO.builder()
                                .id(project.getId())
                                .author(userRepository.getUserName(project.getAuthor()))
                                .regDate(project.getRegDate())
                                .lastModifier(userRepository.getUserName(project.getLastModifier()))
                                .lastModifiedDate(project.getLastModifiedDate())
                                .name(project.getName())
                                .description(project.getDescription())
                                .status(project.getStatus())
                                .finishDate(project.getFinishDate())
                                .manager(UserDTO.builder()
                                        .name(managerName)
                                        .build())
                                .coder(UserDTO.builder()
                                        .name(coderName)
                                        .build())
                                .tester(UserDTO.builder()
                                        .name(testerName)
                                        .build())
                                .primaryLang(project.getPrimaryLang())
                                .build()
                );
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }

    private ProjectDTO plainMap(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .author(userRepository.getUserName(project.getAuthor()))
                .regDate(project.getRegDate())
                .lastModifier(userRepository.getUserName(project.getLastModifier()))
                .lastModifiedDate(project.getLastModifiedDate())
                .name(project.getName())
                .finishDate(project.getFinishDate())
                .status(project.getStatus())
                .manager(UserDTO.builder()
                        //.id(project.getManager())
                        .name(userService.getName(project.getManager()))
                        .build())
                .coder(UserDTO.builder()
                        // .id(project.getManager())
                        .name(userService.getName(project.getCoder()))
                        .build())
                .tester(UserDTO.builder()
                        //.id(project.getManager())
                        .name(userService.getName(project.getCoder()))
                        .build())
                .build();
    }

    private Project buildEntity(ProjectDTO dto) {
        Project doc = new Project();
        doc.setName(dto.getName());
        doc.setStatus(dto.getStatus());
        doc.setStartDate(dto.getStartDate());
        doc.setFinishDate(dto.getFinishDate());
        doc.setPrimaryLang(dto.getPrimaryLang());
        doc.setManager(userService.resolveIdentifier(dto.getManager().getIdentifier()));
        doc.setCoder(userService.resolveIdentifier(dto.getCoder().getIdentifier()));
        doc.setTester(userService.resolveIdentifier(dto.getTester().getIdentifier()));
        doc.setDescription(dto.getDescription());
        return doc;
    }


}
