package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
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
        private Map<LanguageCode, String> locName = new HashMap<>();
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

        public Builder setLocName(Map<LanguageCode, String> locName) {
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
            TaskType newNode = new TaskType();
            newNode.setId(id);
            newNode.setIdentifier(identifier);
            newNode.setLocalizedName(locName);
            newNode.setAuthor(author);
            newNode.setRegDate(regDate);
            newNode.setLastModifier(lastModifier);
            newNode.setLastModifiedDate(lastModifiedDate);
            return newNode;
        }
    }

}
