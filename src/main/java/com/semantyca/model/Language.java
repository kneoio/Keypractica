package com.semantyca.model;

import com.semantyca.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class Language extends DataEntity<String> {
    private String identifier;
    protected String name;
    private Map<LanguageCode, String> localizedNames = new HashMap<>();
    private LanguageCode code = LanguageCode.UNKNOWN;
    private boolean isOn;
    private int position;
    private boolean isCyrillic;

    public static class Builder {
        private String code = LanguageCode.ENG.toString();
        private Map<LanguageCode, String> localizedNames = Map.of(LanguageCode.ENG, LanguageCode.ENG.getLang());

        public Builder setCode(String code) {
            this.code = code;
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
        public Language build() {
            Language newNode = new Language();
            newNode.setCode(LanguageCode.valueOf(code));
            newNode.setLocalizedNames(localizedNames);
            return newNode;
        }



    }

}
