package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.repository.DepartmentRepository;
import io.kneo.officeframe.repository.EmployeeRepository;
import io.kneo.officeframe.repository.OrganizationRepository;
import io.kneo.officeframe.repository.PositionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeService extends AbstractService<Employee, EmployeeDTO> implements IRESTService<EmployeeDTO> {
    private final EmployeeRepository repository;
    private final OrganizationRepository orgRepository;
    private final DepartmentRepository depRepository;
    private final PositionRepository positionRepository;
    private final PositionService positionService;

    protected EmployeeService() {
        super(null, null);
        this.repository = null;
        this.orgRepository = null;
        this.depRepository = null;
        this.positionRepository = null;
        this.positionService = null;
    }

    @Inject
    public EmployeeService(UserRepository userRepository,
                           UserService userService,
                           EmployeeRepository repository,
                           OrganizationRepository orgRepository,
                           DepartmentRepository depRepository,
                           PositionRepository positionRepository,
                           PositionService positionService) {
        super(userRepository, userService);
        assert repository != null : "EmployeeRepository is null";
        assert orgRepository != null : "OrganizationRepository is null";
        assert depRepository != null : "DepartmentRepository is null";
        assert positionRepository != null : "PositionRepository is null";
        assert positionService != null : "PositionService is null";
        this.repository = repository;
        this.orgRepository = orgRepository;
        this.depRepository = depRepository;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
    }

    public Uni<List<EmployeeDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        assert repository != null;
        Uni<List<Employee>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transformToUni(employees ->
                        Uni.combine().all().unis(
                                employees.stream()
                                        .map(emp -> {
                                            Uni<PositionDTO> positionDTOUni;
                                            if (emp.getPosition() == null) {
                                                positionDTOUni = Uni.createFrom().nullItem();
                                            } else {
                                                assert positionService != null;
                                                positionDTOUni = positionService.getDTO(emp.getPosition())
                                                        .onFailure().recoverWithNull();
                                            }

                                            return positionDTOUni.onItem().transform(position ->
                                                    EmployeeDTO.builder()
                                                            .id(emp.getId())
                                                            .userId(emp.getUser())
                                                            .author(userRepository.getUserName(emp.getAuthor()))
                                                            .regDate(emp.getRegDate())
                                                            .lastModifier(userRepository.getUserName(emp.getLastModifier()))
                                                            .lastModifiedDate(emp.getLastModifiedDate())
                                                            .phone(emp.getPhone())
                                                            .rank(emp.getRank())
                                                            .position(position)
                                                            .localizedName(emp.getLocalizedName())
                                                            .identifier(emp.getIdentifier())
                                                            .build()
                                            );
                                        })
                                        .collect(Collectors.toList())
                        ).with(results -> results.stream()
                                .map(result -> (EmployeeDTO) result)
                                .collect(Collectors.toList()))
                );
    }

    @Override
    public Uni<Integer> getAllCount() {
        assert repository != null;
        return repository.getAllCount();
    }

    public Uni<List<Employee>> search(String keyword) {
        assert repository != null;
        return repository.search(keyword);
    }

    @Override
    public Uni<Optional<EmployeeDTO>> getByIdentifier(String identifier) {
        return null;
    }

    @Override
    public Uni<EmployeeDTO> getDTO(String id, IUser user, LanguageCode language) {
        Uni<Employee> uni;
        if ("current".equals(id)) {
            assert repository != null;
            uni = repository.findByUserId(user.getId());
        } else {
            assert repository != null;
            uni = repository.findById(UUID.fromString(id));
        }
        return uni.onItem().transformToUni(doc -> {
            EmployeeDTO dto = EmployeeDTO.builder()
                    .id(doc.getId())
                    .userId(doc.getUser())
                    .author(userRepository.getUserName(doc.getAuthor()))
                    .regDate(doc.getRegDate())
                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .localizedName(doc.getLocalizedName())
                    .phone(doc.getPhone())
                    .rank(doc.getRank())
                    .identifier(doc.getIdentifier())
                    .build();

            if (doc.getPosition() != null) {
                assert positionService != null;
                return positionService.get(doc.getPosition())
                        .onItem().transform(position -> {
                            dto.setPosition(PositionDTO.builder()
                                    .identifier(position.getIdentifier())
                                    .id(position.getId())
                                    .build());
                            return dto;
                        });
            } else {
                return Uni.createFrom().item(dto);
            }
        });
    }

    public Uni<EmployeeDTO> upsert(String id, EmployeeDTO dto, IUser user) {
        Employee doc = new Employee();
        doc.setIdentifier(dto.getIdentifier());
        doc.setPhone(dto.getPhone());
        doc.setDepartment(dto.getDep().getId());
        doc.setLocalizedName(dto.getLocalizedName());
        doc.setOrganization(dto.getOrg().getId());
        doc.setBirthDate(dto.getBirthDate());
        doc.setPosition(dto.getPosition().getId());
        doc.setRoles(null);
        doc.setRank(dto.getRank());
        doc.setBirthDate(dto.getBirthDate());
        if (id == null) {
            assert repository != null;
            return map(repository.insert(doc, user));
        } else {
            UUID uuid = UUID.fromString(id);
            assert repository != null;
            return map(repository.update(uuid, doc, user));
        }
    }

    @Override
    public Uni<EmployeeDTO> add(EmployeeDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<EmployeeDTO> update(String id, EmployeeDTO dto, IUser user) {
        return null;
    }

    private Uni<EmployeeDTO> map(Uni<Employee> employeeUni) {
        Uni<Position> positionUni = employeeUni.onItem().transformToUni(employee ->
                {
                    assert positionService != null;
                    return positionService.get(employee.getPosition());
                }
        );

        return Uni.combine().all().unis(employeeUni, positionUni).with((emp, position) -> {
            EmployeeDTO dto = EmployeeDTO.builder()
                    .id(emp.getId())
                    .userId(emp.getUser())
                    .author(userRepository.getUserName(emp.getAuthor()))
                    .regDate(emp.getRegDate())
                    .lastModifier(userRepository.getUserName(emp.getLastModifier()))
                    .lastModifiedDate(emp.getLastModifiedDate())
                    .localizedName(emp.getLocalizedName())
                    .phone(emp.getPhone())
                    .rank(emp.getRank())
                    .identifier(emp.getIdentifier())
                    .build();
            dto.setPosition(PositionDTO.builder()
                    .identifier(position.getIdentifier())
                    .id(position.getId())
                    .build());

            return dto;
        });
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id));
    }
}
