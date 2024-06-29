package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class DepartmentService extends AbstractService<Department, DepartmentDTO> implements IRESTService<DepartmentDTO> {
    private final DepartmentRepository repository;

    protected DepartmentService() {
        super(null, null);
        this.repository = null;
    }

    public DepartmentService(UserRepository userRepository, UserService userService, DepartmentRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<DepartmentDTO>> getAll(final int limit, final int offset) {
        Uni<List<Department>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(e ->
                                DepartmentDTO.builder()
                                        .id(e.getId())
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .identifier(e.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }
    @Override
    public Uni<Optional<DepartmentDTO>> getByIdentifier(String identifier) {
        return null;
    }

    @Override
    public Uni<DepartmentDTO> getDTO(String id, IUser user, LanguageCode language) {
       return null;
    }
    @Override
    public Uni<UUID> add(DepartmentDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> update(String id, DepartmentDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }

}
