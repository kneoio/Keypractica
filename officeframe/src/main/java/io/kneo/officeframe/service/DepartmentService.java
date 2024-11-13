package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.DepartmentDTO;
import io.kneo.officeframe.model.Department;
import io.kneo.officeframe.repository.DepartmentRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class DepartmentService extends AbstractService<Department, DepartmentDTO> implements IRESTService<DepartmentDTO> {
    private final DepartmentRepository repository;

    @Inject
    public DepartmentService(UserRepository userRepository, UserService userService, DepartmentRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<DepartmentDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        Uni<List<Department>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<List<DepartmentDTO>> getOfOrg(String orgId, LanguageCode languageCode) {
        Uni<List<Department>> listUni = repository.getOfOrg(UUID.fromString(orgId));
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
    }

    public Uni<Department> get(UUID uuid) {
        return repository.findById(uuid);
    }

    public Uni<Department> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    @Override
    public Uni<DepartmentDTO> getDTOByIdentifier(String identifier) {
        return null;
    }

    @Override
    public Uni<DepartmentDTO> getDTO(UUID id, IUser user, LanguageCode language) {
        return repository.findById(id)
                .onItem().transform(this::mapToDTO);
    }

    public Uni<DepartmentDTO> upsert(String id, DepartmentDTO dto, IUser user, LanguageCode code) {
        Department doc = buildEntity(dto);
        if (id == null) {
            return map(repository.insert(doc, AnonymousUser.build()));
        } else {
            return map(repository.update(UUID.fromString(id), doc, user));
        }
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return repository.delete(UUID.fromString(id))
                .onItem().transform(count -> count);
    }

    private Uni<DepartmentDTO> map(Uni<Department> uniDepartment) {
        return uniDepartment.onItem().transform(this::mapToDTO);
    }

    private DepartmentDTO mapToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .author(userRepository.getUserName(department.getAuthor()).await().atMost(TIMEOUT))
                .regDate(department.getRegDate())
                .lastModifier(userRepository.getUserName(department.getLastModifier()).await().atMost(TIMEOUT))
                .lastModifiedDate(department.getLastModifiedDate())
                .identifier(department.getIdentifier())
                .rank(department.getRank())
                .localizedName(department.getLocalizedName())
                .build();
    }

    private Department buildEntity(DepartmentDTO dto) {
        Department department = new Department();
        department.setIdentifier(dto.getIdentifier());
        department.setRank(dto.getRank());
        department.setLocalizedName(dto.getLocalizedName());

        return department;
    }


}