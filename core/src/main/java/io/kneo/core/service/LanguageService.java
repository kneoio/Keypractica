package io.kneo.core.service;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Language;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.LanguageRepository;
import io.kneo.core.repository.UserRepository;
import io.kneo.core.repository.exception.DocumentModificationAccessException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LanguageService extends AbstractService<Language, LanguageDTO> implements IRESTService<LanguageDTO> {

    private final LanguageRepository repository;

  public LanguageService(UserRepository userRepository, UserService userService, LanguageRepository repository) {
        super(userRepository, userService);
        this.repository = repository;
    }

    public Uni<List<LanguageDTO>> getAll(final int limit, final int offset, LanguageCode languageCode) {
        Uni<List<Language>> listUni = repository.getAll(limit, offset);
        return listUni
                .onItem().transform(taskList -> taskList.stream()
                        .map(doc ->
                                LanguageDTO.builder()
                                        .id(doc.getId())
                                        .author(userRepository.getUserName(doc.getAuthor()))
                                        .regDate(doc.getRegDate())
                                        .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                                        .lastModifiedDate(doc.getLastModifiedDate())
                                        .code(doc.getCode())
                                        .localizedName(doc.getLocalizedName())
                                        .build())
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<Integer> getAllCount() {
        return repository.getAllCount();
    }

    @Override
    public Uni<Optional<LanguageDTO>> getByIdentifier(String identifier) {
        return null;
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
                    .author(userService.getName(language.getAuthor()))
                    .regDate(language.getRegDate())
                    .lastModifier(userService.getName(language.getLastModifier()))
                    .lastModifiedDate(language.getLastModifiedDate())
                    .code(language.getCode())
                    .localizedName(language.getLocalizedName())
                    .build();
        });
    }

    @Override
    public Uni<LanguageDTO> getDTO(String id, IUser user, LanguageCode code) {
        Uni<Optional<Language>> uni = repository.findById(UUID.fromString(id));
        return uni.onItem().transform(languageOpt -> {
            Language language = languageOpt.orElseThrow();
            return LanguageDTO.builder()
                    .id(language.getId())
                    .author(userService.getName(language.getAuthor()))
                    .regDate(language.getRegDate())
                    .lastModifier(userService.getName(language.getLastModifier()))
                    .lastModifiedDate(language.getLastModifiedDate())
                    .code(language.getCode())
                    .localizedName(language.getLocalizedName())
                    .build();
        });
    }

    @Override
    public Uni<LanguageDTO> add(LanguageDTO dto, IUser user) {
        return null;
    }

    @Override
    public Uni<LanguageDTO> update(String id, LanguageDTO dto, IUser user) {
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
