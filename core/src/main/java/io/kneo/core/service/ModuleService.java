package io.kneo.core.service;

import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.ModuleRepository;
import io.kneo.core.repository.UserRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ModuleService extends AbstractService<Module, ModuleDTO>  implements IRESTService<ModuleDTO> {
    private final ModuleRepository repository;

    protected ModuleService() {
        super(null, null);
        this.repository = null;
    }

    @Inject
    public ModuleService(UserRepository userRepository, UserService userService, ModuleRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<ModuleDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        Uni<List<Module>> listUni = repository.getAll(limit, offset);
        return listUni.onItem().transform(list -> list.stream()
                .map(doc ->
                        ModuleDTO.builder()
                                .id(doc.getId())
                                .author(userService.getName(doc.getAuthor()))
                                .regDate(doc.getRegDate())
                                .lastModifier(userService.getName(doc.getLastModifier()))
                                .lastModifiedDate(doc.getLastModifiedDate())
                                .identifier(doc.getIdentifier())
                                .localizedName(doc.getLocalizedName())
                                .localizedDescription(doc.getLocalizedDescription())
                                .build())
                .collect(Collectors.toList()));
    }

    @Override
    public Uni<ModuleDTO> getDTOByIdentifier(String identifier) {
        return null;
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<Integer> getAllCount(Long id) {
        return repository.getAllCount();
    }

    public Uni<List<ModuleDTO>> findAll(String[] defaultModules) {
        return repository.getModules(defaultModules)
                .onItem().transformToUni(modules ->
                        Multi.createFrom().iterable(modules)
                                .onItem().transformToUniAndMerge(moduleOpt ->
                                        moduleOpt.map(module ->
                                                mapToDTO(Uni.createFrom().item(Optional.of(module)))
                                        ).orElse(Uni.createFrom().nullItem())
                                )
                                .collect().asList()
                );
    }

    @Override
    public Uni<ModuleDTO> getDTO(UUID id, IUser user, LanguageCode language) {
        Uni<Optional<Module>> uni = repository.findById(id);
        return mapToDTO( uni);
    }

    @Override
    public Uni<Integer> delete(String id, IUser user)  {
        return null;
    }

    private Uni<ModuleDTO> mapToDTO(Uni<Optional<Module>> uni) {
        return uni.onItem().transform(optional -> {
            Module doc = optional.orElseThrow();
            ModuleDTO dto = new ModuleDTO();
            dto.setId(doc.getId());
            dto.setAuthor(userService.getName(doc.getAuthor()));
            dto.setRegDate(doc.getRegDate());
            dto.setLastModifier(userService.getName(doc.getLastModifier()));
            dto.setLastModifiedDate(doc.getLastModifiedDate());
            dto.setIdentifier(doc.getIdentifier());
            dto.setOn(doc.isOn());
            dto.setLocalizedName(doc.getLocalizedName());
            dto.setLocalizedDescription(doc.getLocalizedDescription());
            return dto;
        });
    }

    public Uni<ModuleDTO> upsert(String id, ModuleDTO dto, IUser user, LanguageCode code) {
        return null;
    }

    public Module update(ModuleDTO dto) {
        Module doc = new Module.Builder()
                .setIdentifier(dto.getIdentifier())
                .build();
        assert repository != null;
        return repository.update(doc);
    }

    public Uni<Integer> delete (String id) {
        assert repository != null;
        return repository.delete(UUID.fromString(id));
    }



}
