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
        return repository.getAll(limit, offset)
                .chain(list -> {
                    List<Uni<DepartmentDTO>> unis = list.stream()
                            .map(this::mapToDTO)
                            .collect(Collectors.toList());
                    return Uni.join().all(unis).andFailFast();
                });
    }

    public Uni<List<DepartmentDTO>> getOfOrg(String orgId, LanguageCode languageCode) {
        return repository.getOfOrg(UUID.fromString(orgId))
                .chain(list -> {
                    List<Uni<DepartmentDTO>> unis = list.stream()
                            .map(this::mapToDTO)
                            .collect(Collectors.toList());
                    return Uni.join().all(unis).andFailFast();
                });
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
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
        return repository.findById(id).chain(this::mapToDTO);
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
        return repository.delete(UUID.fromString(id));
    }

    private Uni<DepartmentDTO> map(Uni<Department> uniDepartment) {
        return uniDepartment.chain(this::mapToDTO);
    }

    private Uni<DepartmentDTO> mapToDTO(Department department) {
        return Uni.combine().all().unis(
                userRepository.getUserName(department.getAuthor()),
                userRepository.getUserName(department.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                DepartmentDTO.builder()
                        .id(department.getId())
                        .author(tuple.getItem1())
                        .regDate(department.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(department.getLastModifiedDate())
                        .identifier(department.getIdentifier())
                        .rank(department.getRank())
                        .localizedName(department.getLocalizedName())
                        .build()
        );
    }

    private Department buildEntity(DepartmentDTO dto) {
        Department department = new Department();
        department.setIdentifier(dto.getIdentifier());
        department.setRank(dto.getRank());
        department.setLocalizedName(dto.getLocalizedName());
        return department;
    }
}