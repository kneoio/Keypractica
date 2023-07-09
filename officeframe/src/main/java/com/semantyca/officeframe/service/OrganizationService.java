package com.semantyca.officeframe.service;


import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.repository.exception.DocumentExistsException;
import com.semantyca.officeframe.dto.OrganizationDTO;
import com.semantyca.officeframe.model.Organization;
import com.semantyca.officeframe.repository.OrganizationRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OrganizationService {
    private static final Logger LOGGER = LoggerFactory.getLogger("OrganizationService");
    @Inject
    private OrganizationRepository repository;

    public Uni<List<Organization>> getAll(final int limit, final int offset, final long userID) {
        return repository.getAll(limit, offset, userID);
    }

    public Organization get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String  add(OrganizationDTO dto) throws DocumentExistsException {
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
