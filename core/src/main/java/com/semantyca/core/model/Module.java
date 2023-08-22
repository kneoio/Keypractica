package com.semantyca.core.model;

import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.cnst.ModuleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Module extends DataEntity<UUID> {
    protected String name;
    protected ModuleType type;
    private Map<LanguageCode, String> localizedNames = new HashMap<>();
    private boolean isOn;
    private int position;

    public static class Builder {
        private String name;
        private Map<LanguageCode, String> localizedNames = Map.of(LanguageCode.ENG, ModuleType.UNKNOWN.getName());
        protected ModuleType type = ModuleType.UNKNOWN;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(ModuleType type) {
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
        public Module build() {
            Module newNode = new Module();
            newNode.setName(name);
            newNode.setType(type);
            newNode.setLocalizedNames(localizedNames);
            return newNode;
        }
    }
}
