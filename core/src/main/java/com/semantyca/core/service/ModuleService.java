package com.semantyca.core.service;

import com.semantyca.core.dto.document.LanguageDTO;
import com.semantyca.core.model.Language;
import com.semantyca.core.model.Module;
import com.semantyca.core.model.user.AnonymousUser;
import com.semantyca.core.repository.ModuleRepository;
import com.semantyca.core.repository.exception.DocumentExistsException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

;

@ApplicationScoped
public class ModuleService {
    @Inject
    private ModuleRepository repository;

    public Uni<List<Module>> getAll(int offset, int size) {
        return repository.getAll();
    }
    public Language get(String id) {
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
