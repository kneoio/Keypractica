package io.kneo.officeframe.service;


import io.kneo.core.model.user.AnonymousUser;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.Employee;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.EmployeeRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EmployeeService {
    private static final Logger LOGGER = LoggerFactory.getLogger("EmployeeService");
    @Inject
    private EmployeeRepository repository;

    public Uni<List<EmployeeDTO>> getAll(final int limit, final int offset) {
        return repository.getAll(limit, offset);
    }

    public Uni<Optional<Employee>> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String  add(OrganizationDTO dto) {
        Organization node = new Organization.Builder()
               // .setName(dto.name())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Organization update(OrganizationDTO dto) {
        Organization user = new Organization.Builder()
            //    .setCode(dto.code())
                .build();
        return repository.update(user);
    }
}
