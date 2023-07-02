package com.semantyca.projects.service;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.model.Language;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.repository.LanguageRepository;
import com.semantyca.repository.exception.DocumentExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class.getSimpleName());
    @Inject
    private LanguageRepository repository;

    public List<Language> getAll() {
        return repository.getAll(100, 0);
    }

    public Language get(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public String  add(LanguageDTO dto) throws DocumentExistsException {
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
}
