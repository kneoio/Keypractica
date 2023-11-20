package io.kneo.core.service;

import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.model.Module;
import io.kneo.core.model.cnst.ModuleType;
import io.kneo.core.model.user.AnonymousUser;
import io.kneo.core.repository.ModuleRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ModuleService extends AbstractService<Module, ModuleDTO> {
    @Inject
    private ModuleRepository repository;

    public Uni<List<ModuleDTO>> getAll(final int limit, final int offset) {
        Uni<List<Module>> listUni = repository.getAll(limit, offset);
        return listUni.onItem().transform(list -> list.stream()
                .map(doc ->
                        ModuleDTO.builder()
                                .id(doc.getId())
                                .author(userService.getUserName(doc.getAuthor()))
                                .regDate(doc.getRegDate())
                                .lastModifier(userService.getUserName(doc.getLastModifier()))
                                .lastModifiedDate(doc.getLastModifiedDate())
                                .identifier(doc.getIdentifier())
                                .localizedName(doc.getLocalizedName())
                                .localizedDescription(doc.getLocalizedDescription())
                                .build())
                .collect(Collectors.toList()));
    }

    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    public Uni<List<Module>> findAll(ModuleType ... defaultModules) {
        return repository.getModules(defaultModules);
    }
    public Uni<ModuleDTO> get(String id) {
        Uni<Optional<Module>> uni = repository.findById(UUID.fromString(id));
        return uni.onItem().transform(optional -> {
            Module doc = optional.orElseThrow();
            ModuleDTO dto = new ModuleDTO();
            dto.setId(doc.getId());
            dto.setAuthor(userService.getUserName(doc.getAuthor()));
            dto.setRegDate(doc.getRegDate());
            dto.setLastModifier(userService.getUserName(doc.getLastModifier()));
            dto.setLastModifiedDate(doc.getLastModifiedDate());
            dto.setIdentifier(doc.getIdentifier());
            dto.setOn(doc.isOn());
            dto.setLocalizedName(doc.getLocalizedName());
            dto.setLocalizedDescription(doc.getLocalizedDescription());
            return dto;
        });
    }

    public Uni<UUID> add(ModuleDTO dto) {
        Module doc = new Module.Builder()
                .setIdentifier(dto.getIdentifier())
                .setLocalizedName(dto.getLocalizedName())
                .setLocalizedDescription(dto.getLocalizedDescription())
                .build();
        return repository.insert(doc, AnonymousUser.ID);
    }

    public Module update(ModuleDTO dto) {
        Module doc = new Module.Builder()
                .setIdentifier(dto.getIdentifier())
                .build();
        return repository.update(doc);
    }

    public int delete (String id) {
        return repository.delete(UUID.fromString(id), AnonymousUser.ID);
    }



}
