package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.DepartmentDTO;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.repository.DepartmentRepository;
import io.kneo.officeframe.repository.EmployeeRepository;
import io.kneo.officeframe.repository.OrganizationRepository;
import io.kneo.officeframe.repository.PositionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@ApplicationScoped
public class EmployeeService extends AbstractService<Employee, EmployeeDTO> implements IRESTService<EmployeeDTO> {
    private static final String CURRENT_KEYWORD = "current";
    private final EmployeeRepository repository;
    private final OrganizationRepository orgRepository;
    private final DepartmentRepository depRepository;
    private final PositionRepository positionRepository;
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final PositionService positionService;

    protected EmployeeService() {
        super(null, null);
        this.repository = null;
        this.orgRepository = null;
        this.depRepository = null;
        this.positionRepository = null;
        this.positionService = null;
        this.organizationService = null;
        this.departmentService = null;
    }

    @Inject
    public EmployeeService(UserRepository userRepository,
                           UserService userService,
                           EmployeeRepository repository,
                           OrganizationRepository orgRepository,
                           DepartmentRepository depRepository,
                           PositionRepository positionRepository,
                           PositionService positionService,
                           OrganizationService organizationService,
                           DepartmentService departmentService) {
        super(userRepository, userService);
        this.repository = repository;
        this.orgRepository = orgRepository;
        this.depRepository = depRepository;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
        this.organizationService = organizationService;
        this.departmentService = departmentService;
    }

    public Uni<List<EmployeeDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        assert repository != null;
        return repository.getAll(limit, offset)
                .chain(employees -> Uni.join().all(
                        employees.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList())
                ).andFailFast());
    }

    public Uni<List<EmployeeDTO>> search(String keyword, LanguageCode languageCode) {
        assert repository != null;
        return repository.search(keyword)
                .chain(employees -> Uni.join().all(
                        employees.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList())
                ).andFailFast());
    }

    @Override
    public Uni<Integer> getAllCount() {
        assert repository != null;
        return repository.getAllCount();
    }

    @Override
    public Uni<EmployeeDTO> getDTOByIdentifier(String identifier) {
        assert repository != null;
        return repository.getByIdentifier(identifier).chain(this::mapToDTO);
    }

    public Uni<Employee> getByUserId(long id) {
        assert repository != null;
        return repository.getByUserId(id);
    }

    @Override
    public Uni<EmployeeDTO> getDTO(UUID id, IUser user, LanguageCode language) {
        Uni<Employee> uni;
        if ("current".equals(id)) {
            assert repository != null;
            uni = repository.getByUserId(user.getId());
        } else {
            assert repository != null;
            uni = repository.getById(id);
        }
        return uni.chain(this::mapToDTO);
    }

    public Uni<EmployeeDTO> getDTOByUserId(long id, LanguageCode language) {
        assert repository != null;
        return repository.getByUserId(id).chain(this::mapToDTO);
    }

    public Uni<EmployeeDTO> upsert(String id, EmployeeDTO dto, IUser user, LanguageCode code) {
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

        assert repository != null;
        if (id == null) {
            return repository.insert(doc, user).chain(this::mapToDTO);
        } else {
            return repository.update(UUID.fromString(id), doc, user).chain(this::mapToDTO);
        }
    }

    public Uni<Integer> delete(String id, IUser user) {
        assert repository != null;
        return repository.delete(UUID.fromString(id));
    }

    private Uni<EmployeeDTO> mapToDTO(Employee doc) {
        Uni<PositionDTO> positionDTOUni = doc.getPosition() == null ?
                Uni.createFrom().nullItem() :
                positionService.getDTO(doc.getPosition()).onFailure().recoverWithNull();

        return Uni.combine().all().unis(
                userRepository.getUserName(doc.getAuthor()),
                userRepository.getUserName(doc.getLastModifier()),
                positionDTOUni
        ).asTuple().chain(tuple -> {
            EmployeeDTO dto = EmployeeDTO.builder()
                    .id(doc.getId())
                    .userId(doc.getUserId())
                    .author(tuple.getItem1())
                    .regDate(doc.getRegDate())
                    .lastModifier(tuple.getItem2())
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .phone(doc.getPhone())
                    .rank(doc.getRank())
                    .position(tuple.getItem3())
                    .localizedName(doc.getLocalizedName())
                    .identifier(doc.getIdentifier())
                    .build();

            List<Uni<?>> unis = new ArrayList<>();

            if (doc.getDepartment() != null) {
                unis.add(departmentService.get(doc.getDepartment())
                        .chain(department -> {
                            dto.setDep(DepartmentDTO.builder()
                                    .id(department.getId())
                                    .identifier(department.getIdentifier())
                                    .localizedName(department.getLocalizedName())
                                    .build());
                            return Uni.createFrom().item(dto);
                        }));
            }

            if (doc.getOrganization() != null) {
                unis.add(organizationService.get(doc.getOrganization())
                        .chain(organization -> {
                            dto.setOrg(OrganizationDTO.builder()
                                    .id(organization.getId())
                                    .identifier(organization.getIdentifier())
                                    .localizedName(organization.getLocalizedName())
                                    .build());
                            return Uni.createFrom().item(dto);
                        }));
            }

            return unis.isEmpty() ?
                    Uni.createFrom().item(dto) :
                    Uni.combine().all().unis(unis).with(ignored -> dto);
        });
    }
}