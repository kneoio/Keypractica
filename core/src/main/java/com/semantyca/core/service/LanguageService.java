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

@ApplicationScoped
public class LanguageService implements IBasicService<LanguageDTO> {
    @Inject
    private LanguageRepository repository;

    public Uni<List<LanguageDTO>> getAll(final int limit, final int offset) {
        return repository.getAll(limit, offset);
    }

    public Uni<Language> findByCode(String code) {
        return repository.findByCode(LanguageCode.valueOf(code));
    }

    public Uni<Language> get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String add(LanguageDTO dto) throws DocumentExistsException {
        Language node = new Language.Builder()
                .setCode(dto.code())
                .setLocalizedNames(dto.localizedNames())
                .build();
        return repository.insert(node, AnonymousUser.ID).toString();
    }

    public Language update(LanguageDTO dto) {
        Language user = new Language.Builder()
                .setCode(dto.code())
                .build();
        return repository.update(user);
    }

    public int delete (String id) {
        return repository.delete(UUID.fromString(id), AnonymousUser.ID);
    }
}
