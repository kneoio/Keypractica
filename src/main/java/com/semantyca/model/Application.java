package com.semantyca.model;

import com.semantyca.localization.LanguageCode;
import com.semantyca.model.constants.ApplicationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class Application extends DataEntity<String> {
    private String identifier;
    protected String name;
    protected ApplicationType type;
    private Map<LanguageCode, String> localizedNames = new HashMap<>();
    private boolean isOn;
    private int position;

    public static class Builder {
        private String name;
        private Map<LanguageCode, String> localizedNames = Map.of(LanguageCode.ENG, ApplicationType.DICTIONARY.getName());
        protected ApplicationType type = ApplicationType.DICTIONARY;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(ApplicationType type) {
            this.type = type;
            return this;
        }

        public Builder addLocalizedName(String code, String name) {
            this.localizedNames.put(LanguageCode.valueOf(code), name);
            return this;
        }

        public Builder setLocalizedNames(Map<LanguageCode, String> languageCodeStringMap) {
            this.localizedNames = languageCodeStringMap;
            return this;
        }
        public Application build() {
            Application newNode = new Application();
            newNode.setName(name);
            newNode.setType(type);
            newNode.setLocalizedNames(localizedNames);
            return newNode;
        }



    }

}
