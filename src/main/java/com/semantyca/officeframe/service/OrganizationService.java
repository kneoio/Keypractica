package com.semantyca.officeframe.service;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.model.Language;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.officeframe.repository.OrganizationRepository;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.model.Project;
import com.semantyca.repository.exception.DocumentExistsException;
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

    public Uni<List<Project>> getAll(final int limit, final int offset, final long userID) {
        return repository.getAll(limit, offset, userID);
    }

    public Project get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String  add(ProjectDTO dto) throws DocumentExistsException {
        Project node = new Project.Builder()
                .setName(dto.name())
                .setCoder(dto.coder())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.code())
                .build();
        return repository.update(user);
    }
}
