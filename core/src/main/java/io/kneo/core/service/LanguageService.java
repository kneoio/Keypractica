package io.kneo.core.service;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Language;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.LanguageRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LanguageService extends AbstractService<Language, LanguageDTO> {
    @Inject
    private LanguageRepository repository;

    public Uni<List<LanguageDTO>> getAll(final int limit, final int offset) {
        Uni<List<Language>> listUni = repository.getAll(limit, offset);
        return listUni.onItem().transform(list -> list.stream()
                .map(language ->
                        LanguageDTO.builder()
                                .id(language.getId())
                                .author(userService.getUserName(language.getAuthor()))
                                .regDate(language.getRegDate())
                                .lastModifier(userService.getUserName(language.getLastModifier()))
                                .lastModifiedDate(language.getLastModifiedDate())
                                .code(language.getCode())
                                .localizedName(language.getLocalizedName())
                                .build())
                .collect(Collectors.toList()));
    }

    public Uni<List<Language>> getAvailable() {
        Uni<List<Language>> listUni = repository.getAvailable();
        return listUni.onItem().transform(list -> list.stream()
                .map(language -> {
                        Language doc = new Language();
                        doc.setPosition(doc.getPosition());
                        doc.setCode(doc.getCode());
                        doc.setLocalizedName(language.getLocalizedName());
                        return doc;
                })
                .collect(Collectors.toList()));
    }

    public Uni<LanguageDTO> findByCode(String code) {
        Uni<Optional<Language>> uni = repository.findByCode(LanguageCode.valueOf(code));
        return uni.onItem().transform(languageOpt -> {
            Language language = languageOpt.orElseThrow();
            return LanguageDTO.builder()
                    .id(language.getId())
                    .author(userService.getUserName(language.getAuthor()))
                    .regDate(language.getRegDate())
                    .lastModifier(userService.getUserName(language.getLastModifier()))
                    .lastModifiedDate(language.getLastModifiedDate())
                    .code(language.getCode())
                    .localizedName(language.getLocalizedName())
                    .build();
        });
    }

    public Uni<LanguageDTO> getDTO(String id, IUser user) {
        Uni<Optional<Language>> uni = repository.findById(UUID.fromString(id));
        return uni.onItem().transform(languageOpt -> {
            Language language = languageOpt.orElseThrow();
            return LanguageDTO.builder()
                    .id(language.getId())
                    .author(userService.getUserName(language.getAuthor()))
                    .regDate(language.getRegDate())
                    .lastModifier(userService.getUserName(language.getLastModifier()))
                    .lastModifiedDate(language.getLastModifiedDate())
                    .code(language.getCode())
                    .localizedName(language.getLocalizedName())
                    .build();
        });
    }

    @Override
    public Uni<UUID> add(LanguageDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> update(String id, LanguageDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) throws DocumentModificationAccessException {
        return null;
    }


    public Uni<Void> delete(String id) {
        return repository.delete(UUID.fromString(id));
    }

}
