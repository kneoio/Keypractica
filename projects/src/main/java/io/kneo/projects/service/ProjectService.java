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

    public Uni<List<ProjectDTO>> getAll(final int limit, final int offset, final long userID) {
        assert repository != null;
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

    @Override
    public Uni<ProjectDTO> getDTO(String uuid, IUser user, LanguageCode code) {
        UUID id = UUID.fromString(uuid);
        assert repository != null;
        Uni<Project> projectUni = repository.findById(id, user.getId());
        return projectUni.onItem().transformToUni(this::map);
    }

    @Override
    public Uni<ProjectDTO> upsert(String id, ProjectDTO dto, IUser user) {
        assert repository != null;
        UUID uuid = UUID.fromString(id);
        if (id == null) {
            return repository.insert(buildEntity(dto), user.getId())
                    .onItem().transformToUni(this::map);
        } else {
            return repository.update(uuid, buildEntity(dto), user)
                    .onItem().transformToUni(this::map);
        }
    }


    private Uni<ProjectDTO> map(Project project) {
        assert employeeService != null;
        Uni<EmployeeDTO> managerUni = employeeService.getById(project.getManager())
                .onFailure(DocumentHasNotFoundException.class).recoverWithNull();
        Uni<EmployeeDTO> coderUni = employeeService.getById(project.getCoder())
                .onFailure(DocumentHasNotFoundException.class).recoverWithNull();
        Uni<EmployeeDTO> testerUni = employeeService.getById(project.getTester())
                .onFailure(DocumentHasNotFoundException.class).recoverWithNull();

        return Uni.combine().all().unis(managerUni, coderUni, testerUni)
                .asTuple()
                .onItem().transform(tuple -> ProjectDTO.builder()
                        .id(project.getId())
                        .author(userRepository.getUserName(project.getAuthor()))
                        .regDate(project.getRegDate())
                        .lastModifier(userRepository.getUserName(project.getLastModifier()))
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
        doc.setManager(userService.resolveIdentifier(dto.getManager().getIdentifier()));
        doc.setCoder(userService.resolveIdentifier(dto.getCoder().getIdentifier()));
        doc.setTester(userService.resolveIdentifier(dto.getTester().getIdentifier()));
        doc.setDescription(dto.getDescription());
        return doc;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        UUID uuid = UUID.fromString(id);
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
                .build();
    }
}
