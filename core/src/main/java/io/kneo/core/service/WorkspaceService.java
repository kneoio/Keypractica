package io.kneo.core.service;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.document.ModuleDTO;
import io.kneo.core.model.Module;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.ModuleRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorkspaceService {
    @Inject
    private LanguageService languageService;

    @Inject
    private ModuleRepository repository;
    @Inject
    protected UserService userService;
    public Uni<List<ModuleDTO>> getAvailableModules(IUser user) {
        Uni<List<Module>> listUni = repository.getAll(0, 0);
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

    public Uni<List<LanguageDTO>> getAvailableLanguages(IUser user) {
        return languageService.getAll(0, 0);
    }
}
