package com.semantyca.service;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.model.Language;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.repository.LanguageRepository;
import com.semantyca.repository.exception.DocumentExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class LanguageService {
    @Inject
    private LanguageRepository repository;

    public List<Language> getAll() {
        return repository.getAll(100, 0);
    }

    public Language get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String add(LanguageDTO dto) throws DocumentExistsException {
        Language node = new Language.Builder()
                .setCode(dto.code())
                .setLocalizedNames(dto.localizedNames())
                .build();
        return repository.insert(node, AnonymousUser.ID);
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
