package io.kneo.core.service;

import io.kneo.core.dto.document.LanguageDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.Language;
import io.kneo.core.model.user.IUser;
import io.kneo.core.repository.LanguageRepository;
import io.kneo.core.repository.UserRepository;
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
    public Uni<LanguageDTO> getDTOByIdentifier(String identifier) {
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
    public Uni<LanguageDTO> getDTO(UUID id, IUser user, LanguageCode code) {
        Uni<Language> uni = repository.findById(id);
        return uni.onItem().transform(language -> {
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
    public Uni<LanguageDTO> upsert(UUID id, LanguageDTO dto, IUser user, LanguageCode code) {
        Language doc = new Language();
        doc.setCode(dto.getCode());
        doc.setIdentifier(String.valueOf(dto.getCode()));
        doc.setOn(true);
        doc.setPosition(dto.getPosition());
        doc.setLocalizedName(dto.getLocalizedName());
        doc.setLocalizedName(dto.getLocalizedName());
        if (id == null) {
            return map(repository.insert(doc, user));
        } else {
            return map(repository.update(id, doc, user));
        }
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id));
    }

    private Uni<LanguageDTO> map(Uni<Language> uni) {
        return uni.onItem().transform(this::mapToDTO);
    }

    private LanguageDTO mapToDTO(Language doc) {
        return LanguageDTO.builder()
                .id(doc.getId())
                .author(userRepository.getUserName(doc.getAuthor()))
                .regDate(doc.getRegDate())
                .lastModifier(userRepository.getUserName(doc.getLastModifier()))
                .lastModifiedDate(doc.getLastModifiedDate())
                .code(doc.getCode())
                .position(doc.getPosition())
                .localizedName(doc.getLocalizedName())
                .build();
    }


}
