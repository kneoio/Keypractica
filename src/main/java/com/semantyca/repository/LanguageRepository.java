package com.semantyca.repository;


import com.semantyca.model.Language;
import com.semantyca.repository.exception.DocumentExistsException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class LanguageRepository {


    public List<Language> getAll(int i, int i1) {
        return null;
    }

    private static <Language> List<Language> resultList(Iterable<Language> result) {
        ArrayList<Language> list = new ArrayList<>();
        result.forEach(list::add);
        return list;
    }

    public Language findById(UUID uuid) {

        return null;
    }

    public Optional<Language> findByValue(String base) {
        return null;
    }

    public String insert(Language node, Long user) throws DocumentExistsException {

        return node.getIdentifier();
    }


    public Language update(Language node) {

        return node;
    }

    public int delete(UUID uuid, long id) {

        return 1;
    }
}
