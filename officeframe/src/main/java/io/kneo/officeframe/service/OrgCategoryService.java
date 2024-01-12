package io.kneo.officeframe.service;

import io.kneo.core.model.user.IUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.officeframe.dto.OrgCategoryDTO;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.repository.OrgCategoryRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrgCategoryService extends AbstractService<OrgCategory, OrgCategoryDTO> implements IRESTService<OrgCategoryDTO> {
    @Inject
    private OrgCategoryRepository repository;

    public Uni<List<OrgCategoryDTO>> getAll(final int limit, final int offset) {
        Uni<List<OrgCategory>> uni = repository.getAll(limit, offset);
        return uni
                .onItem().transform(l -> l.stream()
                        .map(e ->
                                OrgCategoryDTO.builder()
                                        .id(e.getId())
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .identifier(e.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<Optional<OrgCategoryDTO>> getByIdentifier(String identifier) {
        return null;
    }

    @Override
    public Uni<OrgCategoryDTO> getDTO(String uuid, IUser user) {
        Uni<Optional<OrgCategory>> labelUni = repository.findById(UUID.fromString(uuid));
        return labelUni.onItem().transform(this::map);
    }

    private OrgCategoryDTO map(Optional<OrgCategory> labelOpt) {
        OrgCategory label = labelOpt.get();
        return OrgCategoryDTO.builder()
                .author(userRepository.getUserName(label.getAuthor()))
                .regDate(label.getRegDate())
                .lastModifier(userRepository.getUserName(label.getLastModifier()))
                .lastModifiedDate(label.getLastModifiedDate())
                .identifier(label.getIdentifier())
                .build();
    }

    public Uni<UUID> add(OrgCategoryDTO dto, IUser user) {
        return null;
    }
    @Override
    public Uni<Integer> update(String id, OrgCategoryDTO dto, IUser user) {
      return null;
    }

    public Uni<Integer> delete(String id, IUser user) {
        return null;
    }
}
