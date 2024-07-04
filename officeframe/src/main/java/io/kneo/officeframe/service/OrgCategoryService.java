package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
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
    private final OrgCategoryRepository repository;

    @Inject
    public OrgCategoryService(UserRepository userRepository, UserService userService, OrgCategoryRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    @SuppressWarnings("ConstantConditions")
    public Uni<List<OrgCategoryDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
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
    @SuppressWarnings("ConstantConditions")
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<Optional<OrgCategoryDTO>> getByIdentifier(String identifier) {
        return Uni.createFrom().item(Optional.empty());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Uni<OrgCategoryDTO> getDTO(String uuid, IUser user, LanguageCode language) {
        Uni<Optional<OrgCategory>> categoryUni = repository.findById(UUID.fromString(uuid));
        return categoryUni.onItem().transform(this::map);
    }

    private OrgCategoryDTO map(Optional<OrgCategory> categoryOpt) {
        return categoryOpt.map(category -> OrgCategoryDTO.builder()
                .author(userRepository.getUserName(category.getAuthor()))
                .regDate(category.getRegDate())
                .lastModifier(userRepository.getUserName(category.getLastModifier()))
                .lastModifiedDate(category.getLastModifiedDate())
                .identifier(category.getIdentifier())
                .localizedNames(category.getLocalizedName())
                .build()).orElse(null);
    }

    @Override
    public Uni<UUID> add(OrgCategoryDTO dto, IUser user) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<Integer> update(String id, OrgCategoryDTO dto, IUser user) {
        return Uni.createFrom().item(0);
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return Uni.createFrom().item(0);
    }
}