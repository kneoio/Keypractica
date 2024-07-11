package io.kneo.officeframe.service;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
import io.kneo.core.service.UserService;
import io.kneo.officeframe.dto.OrgCategoryDTO;
import io.kneo.officeframe.dto.OrganizationDTO;
import io.kneo.officeframe.model.OrgCategory;
import io.kneo.officeframe.model.Organization;
import io.kneo.officeframe.repository.OrgCategoryRepository;
import io.kneo.officeframe.repository.OrganizationRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationService extends AbstractService<Organization, OrganizationDTO> implements IRESTService<OrganizationDTO> {
    private final OrganizationRepository repository;
    private final OrgCategoryRepository orgCategoryRepository;


    @Inject
    public OrganizationService(UserRepository userRepository,
                               UserService userService,
                               OrganizationRepository repository,
                               OrgCategoryRepository orgCategoryRepository) {
        super(userRepository, userService);
        this.repository = repository;
        this.orgCategoryRepository = orgCategoryRepository;
    }

    @SuppressWarnings("ConstantConditions")
    public Uni<List<OrganizationDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        Uni<List<Organization>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(e ->
                                OrganizationDTO.builder()
                                        .id(e.getId())
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .localizedName(e.getLocalizedName())
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
    public Uni<Optional<OrganizationDTO>> getByIdentifier(String identifier) {
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public Uni<Organization> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Uni<OrganizationDTO> getDTO(String id, IUser user, LanguageCode language) {
        return map(repository.findById(UUID.fromString(id)));
    }

    @Override
    public Uni<OrganizationDTO> upsert(String id, OrganizationDTO dto, IUser user) {
        Organization doc = new Organization();
        doc.setIdentifier(dto.getIdentifier());
        doc.setOrgCategory(dto.getOrgCategory().getId());
        doc.setBizID(dto.getBizID());
        doc.setRank(dto.getRank());
        doc.setLocalizedName(dto.getLocalizedName());
        if (id == null) {
            return map(repository.insert(doc, AnonymousUser.build()));
        } else {
            UUID uuid = UUID.fromString(id);
            return map(repository.update(uuid, doc, user));
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Uni<OrganizationDTO> add(OrganizationDTO dto, IUser user) {
        Organization doc = new Organization();
        doc.setIdentifier(dto.getIdentifier());
        doc.setOrgCategory(dto.getOrgCategory().getId());
        doc.setBizID(dto.getBizID());
        doc.setRank(dto.getRank());
        doc.setLocalizedName(dto.getLocalizedName());
        return map(repository.insert(doc, user));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Uni<OrganizationDTO> update(String id, OrganizationDTO dto, IUser user) {
        Organization doc = new Organization();
        doc.setIdentifier(dto.getIdentifier());
        doc.setOrgCategory(dto.getOrgCategory().getId());
        doc.setBizID(dto.getBizID());
        doc.setRank(dto.getRank());
        doc.setLocalizedName(dto.getLocalizedName());
        return map(repository.update(UUID.fromString(id), doc, user));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id))
                .onItem().transform(count -> count);
    }

    private Uni<OrganizationDTO> map(Uni<Organization> uniOrganization) {
        Uni<OrgCategory> relatedUni = uniOrganization.onItem().transformToUni(organization ->
                orgCategoryRepository.findById(organization.getOrgCategory())
        );

        return Uni.combine().all().unis(uniOrganization, relatedUni)
                .combinedWith((organization, category) -> {
                    OrganizationDTO dto = OrganizationDTO.builder()
                            .id(organization.getId())
                            .author(userRepository.getUserName(organization.getAuthor()))
                            .regDate(organization.getRegDate())
                            .lastModifier(userRepository.getUserName(organization.getLastModifier()))
                            .lastModifiedDate(organization.getLastModifiedDate())
                            .identifier(organization.getIdentifier())
                            .localizedName(organization.getLocalizedName())
                            .bizID(organization.getBizID())
                            .build();

                        dto.setOrgCategory(OrgCategoryDTO.builder()
                                .identifier(category.getIdentifier())
                                .localizedName(category.getLocalizedName(LanguageCode.ENG))
                                .id(category.getId())
                                .build());


                    return dto;
                });
    }

}
