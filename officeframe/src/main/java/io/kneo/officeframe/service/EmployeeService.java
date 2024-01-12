package io.kneo.officeframe.service;

import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.RoleRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Department;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.repository.DepartmentRepository;
import io.kneo.officeframe.repository.EmployeeRepository;
import io.kneo.officeframe.repository.OrganizationRepository;
import io.kneo.officeframe.repository.PositionRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeService extends AbstractService<Employee, EmployeeDTO> implements IRESTService<EmployeeDTO> {
    @Inject
    private EmployeeRepository repository;
    @Inject
    private OrganizationRepository organizationRepository;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private PositionRepository positionRepository;
    @Inject
    private RoleRepository roleRepository;
    @Inject
    private PositionService positionService;

    public Uni<List<EmployeeDTO>> getAll(final int limit, final int offset) {
        Uni<List<Employee>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transformToUni(employees ->
                        Uni.combine().all().unis(
                                employees.stream().map(emp ->
                                        positionService.getDTO(emp.getPosition())
                                                .onFailure(NotFoundException.class).recoverWithItem(PositionDTO.builder()
                                                        .identifier("undefined")
                                                        .build())
                                                .onItem().transformToUni(position ->
                                                        Uni.createFrom().item(
                                                                EmployeeDTO.builder()
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
                                                                        .identifier(emp.getIdentifier())
                                                                        .build()
                                                        )
                                                )
                                ).collect(Collectors.toList())
                        ).combinedWith(results -> results.stream().map(result -> (EmployeeDTO) result).collect(Collectors.toList()))
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
    public Uni<EmployeeDTO> getDTO(String id, IUser user) {
        Uni<Optional<Employee>> uni;
        if ("current".equals(id)) {
            uni = repository.findByUserId(user.getId());
        } else {
            uni = repository.findById(UUID.fromString(id));
        }
        Uni<Optional<Position>> positionUni = uni.onItem().transformToUni(item ->
                positionService.get(item.get().getPosition())
        );
        return Uni.combine().all().unis(uni, positionUni).combinedWith((docOpt, positionOpt) -> {
            Employee doc = docOpt.orElseThrow();
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
            if (positionOpt.isPresent()) {
                Position position = positionOpt.get();
                dto.setPosition(PositionDTO.builder()
                        .identifier(position.getIdentifier())
                        .id(position.getId())
                        .build());
            }
            return dto;
        });
    }

    public Uni<UUID> add(EmployeeDTO dto, IUser user) {
        Uni<Optional<Organization>> orgUni = organizationRepository.findById(dto.getId());
        Uni<Optional<Department>> depUni = departmentRepository.findById(dto.getId());
        Uni<Optional<Position>> positionUni = positionRepository.findById(dto.getId());
        //Uni<List<Role>> roleUni = Uni.createFrom().item(new ArrayList<>());
        return Uni.combine().all().unis(orgUni, depUni, positionUni).combinedWith((orgOpt, depOpt, posOpt) -> {
            Employee doc = new Employee();
            doc.setName(dto.getName());
            doc.setUser(dto.getUserId());
            doc.setBirthDate(dto.getBirthDate());
            doc.setPhone(dto.getPhone());
            doc.setRank(dto.getRank());
            doc.setLocalizedName(dto.getLocalizedName());
            orgOpt.ifPresent(org -> doc.setOrganization(org.getId()));
            depOpt.ifPresent(dep -> doc.setDepartment(dep.getId()));
            posOpt.ifPresent(pos -> doc.setPosition(pos.getId()));
            return repository.insert(doc, user.getId());
        }).flatMap(uni -> uni);
    }
    @Override
    public Uni<Integer> update(EmployeeDTO dto, IUser user) {
        Uni<Optional<Organization>> orgUni = organizationRepository.findById(dto.getId());
        Uni<Optional<Department>> depUni = departmentRepository.findById(dto.getId());
        Uni<Optional<Position>> positionUni = positionRepository.findById(dto.getId());
        //Uni<List<Role>> roleUni = Uni.createFrom().item(new ArrayList<>());
        return Uni.combine().all().unis(orgUni, depUni, positionUni).combinedWith((orgOpt, depOpt, posOpt) -> {
            Employee doc = new Employee();
            doc.setName(dto.getName());
            doc.setUser(dto.getUserId());
            doc.setBirthDate(dto.getBirthDate());
            doc.setPhone(dto.getPhone());
            doc.setRank(dto.getRank());
            doc.setLocalizedName(dto.getLocalizedName());
            orgOpt.ifPresent(org -> doc.setOrganization(org.getId()));
            depOpt.ifPresent(dep -> doc.setDepartment(dep.getId()));
            posOpt.ifPresent(pos -> doc.setPosition(pos.getId()));
            return repository.update(doc, user.getId());
        }).flatMap(uni -> uni);
    }

    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id));
    }

}
