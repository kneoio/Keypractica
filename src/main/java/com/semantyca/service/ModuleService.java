package com.semantyca.service;

import com.semantyca.dto.document.LanguageDTO;
import com.semantyca.model.Language;
import com.semantyca.model.Module;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.repository.ModuleRepository;
import com.semantyca.repository.exception.DocumentExistsException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ModuleService {
    @Inject
    private ModuleRepository repository;

    public Uni<List<Module>> getAll() {
        return repository.getAll();
    }

    public Language get(String id) {
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
