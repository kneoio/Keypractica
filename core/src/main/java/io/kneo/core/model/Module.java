package io.kneo.core.model;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.cnst.ModuleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Module  extends SimpleReferenceEntity {
    protected ModuleType type;
    private Map<LanguageCode, String> localizedDescription = new HashMap<>();
    private boolean isOn;

    public static class Builder {
        protected UUID id;
        protected long author;
        private ZonedDateTime regDate;
        protected String identifier;
        private boolean isOn;
        private Map<LanguageCode, String> localizedName = new HashMap<>();
        private Map<LanguageCode, String> localizedDescription = new HashMap<>();
        protected ModuleType type = ModuleType.UNKNOWN;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setAuthor(long author) {
            this.author = author;
            return this;
        }

        public Builder setOn(boolean on) {
            isOn = on;
            return this;
        }

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setRegDate(ZonedDateTime regDate) {
            this.regDate = regDate;
            return this;
        }

        public Builder setType(ModuleType type) {
            this.type = type;
            return this;
        }

        public Builder addLocalizedName(String code, String name) {
            this.localizedName.put(LanguageCode.valueOf(code), name);
            return this;
        }

        public Builder setLocalizedName(Map<LanguageCode, String> languageCodeStringMap) {
            this.localizedName = languageCodeStringMap;
            return this;
        }

        public Builder setLocalizedDescription(Map<LanguageCode, String> localizedDescription) {
            this.localizedDescription = localizedDescription;
            return this;
        }


        public Module build() {
            Module module = new Module();
            module.setId(id);
            module.setAuthor(author);
            module.setRegDate(regDate);
            module.setIdentifier(identifier);
            module.setLocalizedName(localizedName);
            module.setOn(isOn);
            module.setType(type);
            module.setLocalizedDescription(localizedDescription);
            return module;
        }
    }
}
