package io.kneo.mocks;

import io.kneo.core.model.Language;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LanguageServiceMock {

    private List<Language> languages;

    public LanguageServiceMock() {
        this.languages = new ArrayList<>();
      /*  languages.add(new Language("en", "English"));
        languages.add(new Language("es", "Spanish"));
        languages.add(new Language("fr", "French"));*/
    }


    public List<Language> getLanguages() {
        return languages;
    }
}
