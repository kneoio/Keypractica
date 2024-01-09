package io.kneo.officeframe.service;

import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.dto.PositionDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.EmployeeRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeService  extends AbstractService<Employee, EmployeeDTO> implements IRESTService<EmployeeDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger("EmployeeService");
    @Inject
    private EmployeeRepository repository;
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
    public Uni<EmployeeDTO> getDTO(String id) {
        return null;
    }
    public Uni<UUID> add(EmployeeDTO dto) {
        Employee doc = new Employee();
        doc.setName(dto.getName());
        doc.setUser(dto.getUserId());
        doc.setBirthDate(dto.getBirtDate());
        doc.setPhone(dto.getPhone());
        //doc.setOrganization(dto.getName());
        //doc.setDepartment(dto.getName());
        //doc.setPosition(dto.getName());
        //doc.setRoles(dto.getName());
        doc.setRank(dto.getRank());
        return repository.insert(doc, AnonymousUser.ID);
    }

    public Organization update(OrganizationDTO dto) {
        Organization user = new Organization.Builder()
            //    .setCode(dto.code())
                .build();
        return repository.update(user);
    }



}
