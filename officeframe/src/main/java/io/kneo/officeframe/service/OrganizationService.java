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
                                        .isPrimary(e.isPrimary())
                                        .localizedName(e.getLocalizedName())
                                        .identifier(e.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<List<OrganizationDTO>> getPrimary(LanguageCode languageCode) {
        Uni<List<Organization>> listUni = repository.getAllPrimary();
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(e ->
                                OrganizationDTO.builder()
                                        .id(e.getId())
                                        .author(userRepository.getUserName(e.getAuthor()))
                                        .regDate(e.getRegDate())
                                        .lastModifier(userRepository.getUserName(e.getLastModifier()))
                                        .lastModifiedDate(e.getLastModifiedDate())
                                        .isPrimary(e.isPrimary())
                                        .localizedName(e.getLocalizedName())
                                        .identifier(e.getIdentifier())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<OrganizationDTO> getDTOByIdentifier(String identifier) {
        return null;
    }


    public Uni<Organization> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public Uni<Organization> get(UUID uuid) {
        return repository.findById(uuid);
    }

    @Override
    public Uni<OrganizationDTO> getDTO(UUID id, IUser user, LanguageCode language) {
        return map(repository.findById(id));
    }

    @Override
    public Uni<OrganizationDTO> upsert(String id, OrganizationDTO dto, IUser user, LanguageCode code) {
        Organization doc = new Organization();
        doc.setIdentifier(dto.getIdentifier());
        doc.setOrgCategory(dto.getOrgCategory().getId());
        doc.setBizID(dto.getBizID());
        doc.setRank(dto.getRank());
        doc.setPrimary(dto.isPrimary());
        doc.setLocalizedName(dto.getLocalizedName());
        if (id == null) {
            return map(repository.insert(doc, AnonymousUser.build()));
        } else {
            return map(repository.update(UUID.fromString(id), doc, user));
        }
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id))
                .onItem().transform(count -> count);
    }

    private Uni<OrganizationDTO> map(Uni<Organization> uniOrganization) {
        Uni<OrgCategory> relatedUni = uniOrganization.onItem().transformToUni(organization ->
                orgCategoryRepository.findById(organization.getOrgCategory())
        );

        return Uni.combine().all().unis(uniOrganization, relatedUni).with((org, category) -> {
            OrganizationDTO dto = OrganizationDTO.builder()
                    .id(org.getId())
                    .author(userRepository.getUserName(org.getAuthor()))
                    .regDate(org.getRegDate())
                    .lastModifier(userRepository.getUserName(org.getLastModifier()))
                    .lastModifiedDate(org.getLastModifiedDate())
                    .isPrimary(org.isPrimary())
                    .identifier(org.getIdentifier())
                    .localizedName(org.getLocalizedName())
                    .bizID(org.getBizID())
                    .build();

            dto.setOrgCategory(OrgCategoryDTO.builder()
                    .identifier(category.getIdentifier())
                    .localizedNames(category.getLocalizedName())
                    .id(category.getId())
                    .build());


            return dto;
        });
    }

}
