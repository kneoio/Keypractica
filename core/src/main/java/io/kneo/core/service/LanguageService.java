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
        return repository.getAll(limit, offset)
                .chain(list -> {
                    List<Uni<LanguageDTO>> unis = list.stream()
                            .map(this::mapToDTO)
                            .collect(Collectors.toList());
                    return Uni.join().all(unis).andFailFast();
                });
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
        return repository.findByCode(LanguageCode.valueOf(code))
                .chain(languageOpt -> mapToDTO(languageOpt.orElseThrow()));
    }

    @Override
    public Uni<LanguageDTO> getDTO(UUID id, IUser user, LanguageCode code) {
        return repository.findById(id).chain(this::mapToDTO);
    }

    @Override
    public Uni<LanguageDTO> upsert(String id, LanguageDTO dto, IUser user, LanguageCode code) {
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
            return map(repository.update(UUID.fromString(id), doc, user));
        }
    }

    @Override
    public Uni<Integer> delete(String id, IUser user) {
        return repository.delete(UUID.fromString(id));
    }

    private Uni<LanguageDTO> map(Uni<Language> uni) {
        return uni.chain(this::mapToDTO);
    }

    private Uni<LanguageDTO> mapToDTO(Language doc) {
        return Uni.combine().all().unis(
                userRepository.getUserName(doc.getAuthor()),
                userRepository.getUserName(doc.getLastModifier())
        ).asTuple().onItem().transform(tuple ->
                LanguageDTO.builder()
                        .id(doc.getId())
                        .author(tuple.getItem1())
                        .regDate(doc.getRegDate())
                        .lastModifier(tuple.getItem2())
                        .lastModifiedDate(doc.getLastModifiedDate())
                        .code(doc.getCode())
                        .position(doc.getPosition())
                        .localizedName(doc.getLocalizedName())
                        .build()
        );
    }
}