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
import java.util.UUID;
import java.util.stream.Collectors;
@ApplicationScoped
public class ModuleService extends AbstractService<Module, ModuleDTO> implements IRESTService<ModuleDTO> {
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
        return repository.getAll(limit, offset)
                .chain(list -> {
                    List<Uni<ModuleDTO>> unis = list.stream()
                            .map(this::mapToDTO)
                            .collect(Collectors.toList());
                    return Uni.join().all(unis).andFailFast();
                });
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
                .chain(modules ->
                        Multi.createFrom().iterable(modules)
                                .onItem().transformToUniAndMerge(moduleOpt ->
                                        moduleOpt.map(this::mapToDTO).orElse(Uni.createFrom().nullItem())
                                )
                                .collect().asList()
                );
    }

    @Override
    public Uni<ModuleDTO> getDTO(UUID id, IUser user, LanguageCode language) {
        return repository.findById(id)
                .chain(optional -> mapToDTO(optional.orElseThrow()));
    }

    private Uni<ModuleDTO> mapToDTO(Module doc) {
        return Uni.combine().all().unis(
                userService.getName(doc.getAuthor()),
                userService.getName(doc.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                ModuleDTO.builder()
                        .id(doc.getId())
                        .author(tuple.getItem1())
                        .regDate(doc.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(doc.getLastModifiedDate())
                        .identifier(doc.getIdentifier())
                        .isOn(doc.isOn())
                        .localizedName(doc.getLocalizedName())
                        .localizedDescription(doc.getLocalizedDescription())
                        .build()
        );
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return null;
    }

    @Override
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

    public Uni<Integer> delete(String id) {
        assert repository != null;
        return repository.delete(UUID.fromString(id));
    }
}