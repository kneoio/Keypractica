package com.semantyca.projects.service;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.model.Language;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.repository.ProjectRepository;
import com.semantyca.repository.UserRepository;
import com.semantyca.repository.exception.DocumentExistsException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProjectService");
    @Inject
    private ProjectRepository repository;

    @Inject
    private UserRepository userRepository;

    public Uni<List<ProjectDTO>> getAll(final int limit, final int offset, final long userID) {
        return repository.getAll(limit, offset, userID);
    }

    public Uni<ProjectDTO> get(String id) {
        return repository.findById(UUID.fromString(id), 2L).onItem()
                .transform(p -> new ProjectDTO(p.getId(),p.getName(), p.getStatus(), p.getFinishDate(), userRepository.getName(p.getManager())));

    }

    public String  add(ProjectDTO dto) throws DocumentExistsException {
        Project node = new Project.Builder()
                .setName(dto.name())
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
