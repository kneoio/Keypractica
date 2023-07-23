package com.semantyca.core.service;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.repository.LanguageRepository;
import com.semantyca.core.repository.exception.DocumentExistsException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LanguageService implements IBasicService<LanguageDTO> {
    @Inject
    private LanguageRepository repository;

    @Inject
    private UserService userService;

    public Uni<List<LanguageDTO>> getAll(final int limit, final int offset) {
        Uni<List<Language>> langUni = repository.getAll(limit, offset);
        return langUni.onItem().transform(list -> list.stream()
                        .map(language ->
                                LanguageDTO.builder()
                                        .id(language.getId())
                                        .author(userService.getUserName(language.getAuthor()))
                                        .regDate(language.getRegDate())
                                        .lastModifier(userService.getUserName(language.getLastModifier()))
                                        .lastModifiedDate(language.getLastModifiedDate())
                                        .code(LanguageCode.valueOf(language.getCode()))
                                        .localizedNames(language.getLocalizedNames())
                                        .build())
                        .collect(Collectors.toList()));
    }

    public Uni<Language> findByCode(String code) {
        return repository.findByCode(LanguageCode.valueOf(code));
    }

    public Uni<Language> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String add(LanguageDTO dto) throws DocumentExistsException {
        Language node = new Language.Builder()
                .setCode(dto.getCode().toString())
                .setLocalizedNames(dto.getLocalizedNames())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.getCode().toString())
                .build();
        return repository.update(user);
    }

    public int delete (String id) {
        return repository.delete(UUID.fromString(id), AnonymousUser.ID);
    }
}
