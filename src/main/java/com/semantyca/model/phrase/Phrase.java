package com.semantyca.model.phrase;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.model.SecureDataEntity;
import com.semantyca.model.embedded.RLSEntry;
import com.semantyca.model.user.AnonymousUser;
import com.semantyca.repository.glossary.Label;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Phrase extends SecureDataEntity<UUID> {
    private String base;
    private String translation;
    private String basePronunciation;
    private String translationPronunciation;

    private List<Label> labels = new ArrayList<>();

    public Phrase() {
    }

    public Phrase(String base, String translation) {
        this.base = base;
        this.translation = translation;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getBasePronunciation() {
        return basePronunciation;
    }

    public void setBasePronunciation(String basePronunciation) {
        this.basePronunciation = basePronunciation;
    }

    public String getTranslationPronunciation() {
        return translationPronunciation;
    }

    public void setTranslationPronunciation(String translationPronunciation) {
        this.translationPronunciation = translationPronunciation;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    @Override
    public UUID getIdentifier() {
        return null;
    }

    @Override
    public void setAuthor(long author) {

    }

    public static class Builder {
        private String id;
        private Long author = AnonymousUser.ID;
        private LocalDateTime regDate = LocalDateTime.now();
        private LocalDateTime lastModifiedDate = LocalDateTime.now();
        private Long lastModifier = AnonymousUser.ID;
        private String title = "";
        private String base;
        private String translation;
        private String basePronunciation;
        private String translationPronunciation;

        private List<Label> labels = new ArrayList<>();
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setBase(String base) {
            this.base = base;
            return this;
        }

        public Builder setTranslation(String translation) {
            this.translation = translation;
            return this;
        }

        public Builder setBasePronunciation(String basePronunciation) {
            this.basePronunciation = basePronunciation;
            return this;
        }

        public Builder setTranslationPronunciation(String translationPronunciation) {
            this.translationPronunciation = translationPronunciation;
            return this;
        }

        public Builder setLabels(List<String> l) {
            l.stream().forEach(v -> labels.add(new Label(v)));
            return this;
        }

        public Phrase build() {
            Phrase entity = new Phrase();
            if (id != null) {
             //   entity.setId(UUID.fromString(id));
            }
            entity.setRegDate(regDate);
            entity.setAuthor(author);
            RLSEntry rlsEntry = new RLSEntry();
            rlsEntry.allowEdit();
            rlsEntry.setReader(author);
            entity.setLastModifiedDate(lastModifiedDate);
            entity.setLastModifier(lastModifier);
            entity.addReader(rlsEntry);
            entity.setTitle(title);
            entity.setBase(base);
            entity.setTranslation(translation);
            entity.setBasePronunciation(basePronunciation);
            entity.setTranslationPronunciation(translationPronunciation);
            entity.setLabels(labels);
            return entity;
        }
    }
}
