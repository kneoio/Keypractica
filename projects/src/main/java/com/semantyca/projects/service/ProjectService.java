package com.semantyca.projects.service;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.model.user.IUser;
import com.semantyca.core.repository.UserRepository;
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
public class ProjectService {
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

    public Uni<ProjectDTO> get(String uuid) {
        UUID id = UUID.fromString(uuid);
        Uni<Optional<Project>> projectUni = repository.findById(id, 2L);
        Uni<Optional<IUser>> manager = projectUni.onItem().transformToUni(item ->
                userRepository.findById(item.get().getManager())
        );
        Uni<Optional<IUser>> coder = projectUni.onItem().transformToUni(item ->
                userRepository.findById(item.get().getCoder())
        );
        Uni<Optional<IUser>> tester = projectUni.onItem().transformToUni(item ->
                userRepository.findById(item.get().getTester())
        );

        Uni<List<RLS>> rlsEntires = projectUni.onItem().transformToUni(item ->
                repository.getAllReaders(id)
        );

        return Uni.combine().all().unis(projectUni, manager, coder, tester, rlsEntires).combinedWith((projectOptional, userOptional, coderOptional, testerOtional, rlsList) -> {
                    Project p = projectOptional.get();
                    return new ProjectDTO(p.getId(), p.getName(), p.getStatus(), p.getFinishDate(), userOptional.get().getUserName(), coderOptional.get().getUserName(), testerOtional.get().getUserName(), rlsList);
                }
        );

    }

    public String add(ProjectDTO dto)  {
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
