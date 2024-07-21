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
        Uni<List<Employee>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transformToUni(employees ->
                        Uni.combine().all().unis(
                                employees.stream()
                                        .map(this::createEmployeeDTOUni)
                                        .collect(Collectors.toList())
                        ).with(results -> results.stream()
                                .map(result -> (EmployeeDTO) result)
                                .collect(Collectors.toList()))
                );
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<List<Employee>> search(String keyword) {
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
            uni = repository.findByUserId(user.getId());
        } else {
            uni = repository.findById(UUID.fromString(id));
        }
        Uni<Position> positionUni = uni.onItem().transformToUni(item ->
                positionService.get(item.getPosition())
        );
        return Uni.combine().all().unis(uni, positionUni).with((doc, position) -> {
            EmployeeDTO dto = EmployeeDTO.builder()
                    .id(doc.getId())
                    .userId(doc.getUser())
                    .author(userRepository.getUserName(doc.getAuthor()))
                    .regDate(doc.getRegDate())
                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .name(doc.getName())
                    .localizedName(doc.getLocalizedName())
                    .phone(doc.getPhone())
                    .rank(doc.getRank())
                    .identifier(doc.getIdentifier())
                    .build();
            dto.setPosition(PositionDTO.builder()
                    .identifier(position.getIdentifier())
                    .id(position.getId())
                    .build());
            return dto;
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
            return map(repository.insert(doc, user));
        } else {
            UUID uuid = UUID.fromString(id);
            return map(repository.update(uuid, doc, user));
        }
    }

    @Override
    public Uni<EmployeeDTO> add(EmployeeDTO dto, IUser user) {
        Employee doc = new Employee();
        doc.setName(dto.getName());
        doc.setIdentifier(constructIdentifier(dto.getName()));
        doc.setUser(dto.getUserId());
        doc.setBirthDate(dto.getBirthDate());
        doc.setPhone(dto.getPhone());
        doc.setRank(dto.getRank());
        doc.setLocalizedName(dto.getLocalizedName());
        doc.setOrganization(dto.getOrg().getId());
        doc.setDepartment(dto.getDep().getId());
        doc.setPosition(dto.getPosition().getId());
        return map(repository.insert(doc, user));
    }

    @Override
    public Uni<EmployeeDTO> update(String id, EmployeeDTO dto, IUser user) {
        Employee doc = new Employee();
        doc.setName(dto.getName());
        doc.setIdentifier(constructIdentifier(dto.getName()));
        doc.setUser(dto.getUserId());
        doc.setBirthDate(dto.getBirthDate());
        doc.setPhone(dto.getPhone());
        doc.setRank(dto.getRank());
        doc.setLocalizedName(dto.getLocalizedName());
        doc.setOrganization(dto.getOrg().getId());
        doc.setDepartment(dto.getDep().getId());
        doc.setPosition(dto.getPosition().getId());
        return map(repository.update(UUID.fromString(id), doc, user));
    }

    private Uni<EmployeeDTO> map(Uni<Employee> employeeUni) {
        Uni<Position> positionUni = employeeUni.onItem().transformToUni(employee ->
                positionService.get(employee.getPosition())
        );

        return Uni.combine().all().unis(employeeUni, positionUni).with((emp, position) -> {
            EmployeeDTO dto = EmployeeDTO.builder()
                    .id(emp.getId())
                    .userId(emp.getUser())
                    .author(userRepository.getUserName(emp.getAuthor()))
                    .regDate(emp.getRegDate())
                    .lastModifier(userRepository.getUserName(emp.getLastModifier()))
                    .lastModifiedDate(emp.getLastModifiedDate())
                    .name(emp.getName())
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
        return repository.delete(UUID.fromString(id));
    }

    protected String getIdentifier(Employee employee) {
        String i = employee.getIdentifier();
        if (i != null) {
            return i;
        } else {
            return employee.getName().toLowerCase().replace(" ", "_");
        }
    }

    protected static String constructIdentifier(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    private Uni<EmployeeDTO> createEmployeeDTOUni(Employee emp) {
        Uni<PositionDTO> positionDTOUni;

        if (emp.getPosition() == null) {
            positionDTOUni = Uni.createFrom().nullItem();
        } else {
            positionDTOUni = positionService.getDTO(emp.getPosition())
                    .onFailure().recoverWithNull();
        }

        return positionDTOUni.onItem().transform(position -> createEmployeeDTO(emp, position));
    }

    private EmployeeDTO createEmployeeDTO(Employee emp, PositionDTO position) {
        return EmployeeDTO.builder()
                .id(emp.getId())
                .userId(emp.getUser())
                .author(userRepository.getUserName(emp.getAuthor()))
                .regDate(emp.getRegDate())
                .lastModifier(userRepository.getUserName(emp.getLastModifier()))
                .lastModifiedDate(emp.getLastModifiedDate())
                .name(emp.getName())
                .phone(emp.getPhone())
                .rank(emp.getRank())
                .position(position)
                .identifier(getIdentifier(emp))
                .build();
    }
}
