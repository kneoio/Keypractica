package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.OrgCategoryDTO;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.model.Position;
import io.kneo.officeframe.repository.OrgCategoryRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
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
        return repository.getAll(limit, offset)
                .chain(list -> Uni.join().all(
                        list.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList())
                ).andFailFast());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<OrgCategoryDTO> getDTOByIdentifier(String identifier) {
        return Uni.createFrom().item(null);
    }

    @Override
    public Uni<OrgCategoryDTO> getDTO(UUID uuid, IUser user, LanguageCode language) {
        return repository.findById(uuid).chain(this::mapToDTO);
    }

    @Override
    public Uni<OrgCategoryDTO> upsert(String id, OrgCategoryDTO dto, IUser user, LanguageCode code) {
        Position doc = new Position();
        doc.setIdentifier(dto.getIdentifier());
        doc.setLocalizedName(dto.getLocalizedName());
        return null;
    }

    private Uni<OrgCategoryDTO> mapToDTO(OrgCategory category) {
        return Uni.combine().all().unis(
                userRepository.getUserName(category.getAuthor()),
                userRepository.getUserName(category.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                OrgCategoryDTO.builder()
                        .author(tuple.getItem1())
                        .regDate(category.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(category.getLastModifiedDate())
                        .identifier(category.getIdentifier())
                        .localizedNames(category.getLocalizedName())
                        .build()
        );
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return Uni.createFrom().item(0);
    }
}