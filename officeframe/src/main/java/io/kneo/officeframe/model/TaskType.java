package io.kneo.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class TaskType extends SimpleReferenceEntity {
    public String prefix;

    public static class Builder {
        private UUID id;
        protected String identifier;
        private EnumMap<LanguageCode, String> locName = new EnumMap<>(LanguageCode.class);
        private long author;
        private ZonedDateTime regDate;
        private ZonedDateTime lastModifiedDate;
        private long lastModifier;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setLocName(EnumMap<LanguageCode, String> locName) {
            this.locName = locName;
            return this;
        }

        public Builder setAuthor(long author) {
            this.author = author;
            return this;
        }

        public Builder setRegDate(ZonedDateTime regDate) {
            this.regDate = regDate;
            return this;
        }

        public Builder setLastModifiedDate(ZonedDateTime lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
            return this;
        }

        public Builder setLastModifier(long lastModifier) {
            this.lastModifier = lastModifier;
            return this;
        }

        public TaskType build() {
            TaskType doc = new TaskType();
            doc.setId(id);
            doc.setIdentifier(identifier);
            doc.setLocalizedName(locName);
            doc.setAuthor(author);
            doc.setRegDate(regDate);
            doc.setLastModifier(lastModifier);
            doc.setLastModifiedDate(lastModifiedDate);
            return doc;
        }
    }

}
