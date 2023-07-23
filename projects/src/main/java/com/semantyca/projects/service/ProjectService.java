package com.semantyca.projects.service;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.repository.UserRepository;
import com.semantyca.core.service.AbstractService;
import com.semantyca.projects.dto.ProjectDTO;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.repository.ProjectRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProjectService extends AbstractService {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProjectService");
    @Inject
    private ProjectRepository repository;
    @Inject
    private UserRepository userRepository;

    public Uni<List<ProjectDTO>> getAll(final int limit, final int offset, final long userID) {
        return repository.getAll(limit, offset, userID);
    }

    public Uni<Integer> getAllCount(final long userID) {
        return repository.getAllCount(userID);
    }

    public Uni<ProjectDTO> get(String uuid, final long userID) {
        return get(UUID.fromString(uuid), userID);
    }

    public Uni<ProjectDTO> get(UUID id, final long userID) {
        Uni<Optional<Project>> projectUni = repository.findById(id, userID);

        Uni<List<RLSDTO>> rlsDtoListUni = getRLSDTO(repository, projectUni, id);

        return Uni.combine().all().unis(projectUni, rlsDtoListUni).combinedWith((projectOptional, rlsList) -> {
                    Project project = projectOptional.orElseThrow();
                    return new ProjectDTO(
                            project.getId(),
                            project.getName(),
                            project.getStatus(),
                            project.getFinishDate(),
                            userRepository.getUserName(project.getManager()),
                            userRepository.getUserName(project.getCoder()),
                            userRepository.getUserName(project.getTester()),
                            rlsList);
                }
        );

    }

    public String add(ProjectDTO dto) {
        Project node = new Project.Builder()
                .setName(dto.name())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.getCode())
                .build();
        return repository.update(user);
    }


}
