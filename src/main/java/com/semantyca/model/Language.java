package com.semantyca.model;

import com.semantyca.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class Language extends DataEntity<UUID> {
    protected String name;
    private Map<LanguageCode, String> localizedNames = new HashMap<>();
    private LanguageCode code = LanguageCode.UNKNOWN;
    private boolean isOn;
    private int position;

    public static class Builder {
        private String code = LanguageCode.ENG.toString();
        private boolean isOn;
        private int position;
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

        public Builder setOn(boolean on) {
            isOn = on;
            return this;
        }

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }


        public Language build() {
            Language newNode = new Language();
            newNode.setCode(LanguageCode.valueOf(code));
            newNode.setOn(isOn);
            newNode.setPosition(position);
            newNode.setLocalizedNames(localizedNames);
            return newNode;
        }



    }

}
