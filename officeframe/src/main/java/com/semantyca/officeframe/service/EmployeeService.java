package com.semantyca.officeframe.service;


import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.officeframe.dto.EmployeeDTO;
import com.semantyca.officeframe.dto.OrganizationDTO;
import com.semantyca.officeframe.model.Employee;
import com.semantyca.officeframe.model.Organization;
import com.semantyca.officeframe.repository.EmployeeRepository;
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
