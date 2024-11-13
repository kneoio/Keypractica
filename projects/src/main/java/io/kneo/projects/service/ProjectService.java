package io.kneo.projects.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentHasNotFoundException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.service.EmployeeService;
import io.kneo.projects.dto.ProjectDTO;
import io.kneo.projects.model.Project;
import io.kneo.projects.repository.ProjectRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Validator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@ApplicationScoped
public class ProjectService extends AbstractService<Project, ProjectDTO> {
    private final ProjectRepository repository;
    private final EmployeeService employeeService;

    Validator validator;

    protected ProjectService() {
        super(null, null);
        this.repository = null;
        this.employeeService = null;
    }

    @Inject
    public ProjectService(UserRepository userRepository, UserService userService, Validator validator, ProjectRepository repository, EmployeeService employeeService) {
        super(userRepository, userService);
        this.validator = validator;
        this.repository = repository;
        this.employeeService = employeeService;
    }

    public Uni<List<ProjectDTO>> getAll(final int limit, final int offset, final IUser user) {
        assert repository != null;
        Uni<List<Project>> uni = repository.getAll(limit, offset, user);
        return uni
                .onItem().transform(projectList -> projectList.stream()
                        .map(project -> {
                            return ProjectDTO.builder()
                                    .id(project.getId())
                                    .author(userRepository.getUserName(project.getAuthor()).await().atMost(TIMEOUT))
                                    .regDate(project.getRegDate())
                                    .lastModifier(userRepository.getUserName(project.getLastModifier()).await().atMost(TIMEOUT))
                                    .lastModifiedDate(project.getLastModifiedDate())
                                    .name(project.getName())
                                    .finishDate(project.getFinishDate())
                                    .status(project.getStatus())
                                    .build();
                        })
                        .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount(final IUser user) {
        assert repository != null;
        return repository.getAllCount(user);
    }

    public Uni<List<Project>> search(String keyword) {
        return repository.search(keyword);
    }

    @Override
    public Uni<ProjectDTO> getDTO(UUID uuid, IUser user, LanguageCode code) {
        assert repository != null;
        Uni<Project> projectUni = repository.findById(uuid, user.getId());
        return projectUni.onItem().transformToUni(this::map);
    }

    public Uni<Project> getById(UUID uuid, IUser user) {
        assert repository != null;
        return repository.findById(uuid, user.getId());
    }

    @Override
    public Uni<ProjectDTO> upsert(String id, ProjectDTO dto, IUser user, LanguageCode code) {
        assert repository != null;
        if (id == null) {
            return repository.insert(buildEntity(dto), user.getId())
                    .onItem().transformToUni(this::map);
        } else {
            return repository.update(UUID.fromString(id), buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        }
    }


    private Uni<ProjectDTO> map(Project project) {
        assert employeeService != null;
        Uni<EmployeeDTO> managerUni = employeeService.getDTOByUserId(project.getManager(), LanguageCode.ENG)
                .onFailure(DocumentHasNotFoundException.class).recoverWithNull();
        Uni<EmployeeDTO> coderUni = employeeService.getDTOByUserId(project.getCoder(), LanguageCode.ENG)
                .onFailure(DocumentHasNotFoundException.class).recoverWithNull();
        Uni<EmployeeDTO> testerUni = employeeService.getDTOByUserId(project.getTester(), LanguageCode.ENG)
                .onFailure(DocumentHasNotFoundException.class).recoverWithNull();

        return Uni.combine().all().unis(managerUni, coderUni, testerUni)
                .asTuple()
                .onItem().transform(tuple -> ProjectDTO.builder()
                        .id(project.getId())
                        .author(userRepository.getUserName(project.getAuthor()).await().atMost(TIMEOUT))
                        .regDate(project.getRegDate())
                        .lastModifier(userRepository.getUserName(project.getLastModifier()).await().atMost(TIMEOUT))
                        .lastModifiedDate(project.getLastModifiedDate())
                        .name(project.getName())
                        .description(project.getDescription())
                        .status(project.getStatus())
                        .finishDate(project.getFinishDate())
                        .manager(tuple.getItem1())
                        .coder(tuple.getItem2())
                        .tester(tuple.getItem3())
                        .primaryLang(project.getPrimaryLang())
                        .build());
    }

    private Project buildEntity(ProjectDTO dto) {
        Project doc = new Project();
        doc.setName(dto.getName());
        doc.setStatus(dto.getStatus());
        doc.setStartDate(dto.getStartDate());
        doc.setFinishDate(dto.getFinishDate());
        doc.setPrimaryLang(dto.getPrimaryLang());
        doc.setManager(userService.resolveIdentifier(dto.getManager().getIdentifier()).await().atMost(TIMEOUT));
        doc.setCoder(userService.resolveIdentifier(dto.getCoder().getIdentifier()).await().atMost(TIMEOUT));
        doc.setTester(userService.resolveIdentifier(dto.getTester().getIdentifier()).await().atMost(TIMEOUT));
        doc.setDescription(dto.getDescription());
        return doc;
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id), user);
    }
}
