package io.kneo.core.service;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.dto.document.UserModuleDTO;
import io.kneo.core.model.UserModule;
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
    public Uni<List<UserModuleDTO>> getAvailableModules(IUser user) {
        Uni<List<UserModule>> listUni = repository.getAvailable(user);
        return listUni.onItem().transform(list -> list.stream()
                .map(doc ->
                        UserModuleDTO.builder()
                                .identifier(doc.getIdentifier())
                                .localizedName(doc.getLocalizedName())
                                .localizedDescription(doc.getLocalizedDescription())
                                .position(doc.getPosition())
                                .theme(doc.getTheme())
                                .build())
                .collect(Collectors.toList()));
    }

    public Uni<List<LanguageDTO>> getAvailableLanguages(IUser user) {
        return languageService.getAll(0, 0);
    }

}
