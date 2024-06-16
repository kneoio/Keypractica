package io.kneo.officeframe.service;

import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.model.user.IUser;
import io.kneo.core.service.AbstractService;
import io.kneo.core.service.IRESTService;
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
    @Inject
    private OrganizationRepository repository;

    @Inject
    OrgCategoryRepository orgCategoryRepository;

    public Uni<List<OrganizationDTO>> getAll(final int limit, final int offset) {
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
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<Optional<OrganizationDTO>> getByIdentifier(String identifier) {
        return null;
    }

    public Uni<Optional<Organization>> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    @Override
    public Uni<OrganizationDTO> getDTO(String id, IUser user) {
        Uni<Optional<Organization>> uni = repository.findById(UUID.fromString(id));

        Uni<Optional<OrgCategory>> relatedUni = uni.onItem().transformToUni(item ->
                orgCategoryRepository.findById(item.get().getOrgCategory())

        );
        return Uni.combine().all().unis(uni, relatedUni).combinedWith((docOpt, orgCategory) -> {
            Organization doc = docOpt.orElseThrow();
            OrganizationDTO dto = OrganizationDTO.builder()
                    .id(doc.getId())
                    .author(userRepository.getUserName(doc.getAuthor()))
                    .regDate(doc.getRegDate())
                    .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                    .lastModifiedDate(doc.getLastModifiedDate())
                    .identifier(doc.getIdentifier())
                    .localizedName(doc.getLocalizedName())
                    .bizID(doc.getBizID())
                    .build();
            if (orgCategory.isPresent()) {
                OrgCategory category = orgCategory.get();
                dto.setOrgCategory(OrgCategoryDTO.builder()
                        .identifier(category.getIdentifier())
                        .id(category.getId())
                        .build());
            }
            return dto;
        });
    }

    @Override
    public Uni<UUID> add(OrganizationDTO dto, IUser user) {
        Organization doc = new Organization();
        doc.setIdentifier(dto.getIdentifier());
        doc.setOrgCategory(dto.getOrgCategory().getId());
        doc.setBizID(dto.getBizID());
        doc.setRank(dto.getRank());
        return repository.insert(doc, AnonymousUser.build());
    }

    @Override
    public Uni<Integer> update(String id, OrganizationDTO dto, IUser user) {
        Organization doc = new Organization();
        doc.setIdentifier(dto.getIdentifier());
        doc.setOrgCategory(dto.getOrgCategory().getId());
        doc.setBizID(dto.getBizID());
        doc.setRank(dto.getRank());
        doc.setLocalizedName(dto.getLocalizedName());
        return repository.update(UUID.fromString(id), doc, user)
                .onItem().transform(count -> count > 0 ? count : null);
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id))
                .onItem().transform(count -> count);
    }
}
