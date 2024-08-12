package io.kneo.core.model;

import io.kneo.core.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Language extends SimpleReferenceEntity {
    private LanguageCode code = LanguageCode.UNKNOWN;
    private boolean isOn;
    private int position;


    public static class Builder extends AbstractEntityBuilder {
        private LanguageCode code = LanguageCode.ENG;
        private boolean isOn;
        private int position = 999;
        private EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setCode(LanguageCode code) {
            this.code = code;
            return this;
        }

        public Builder addLocalizedName(String code, String name) {
            this.localizedName.put(LanguageCode.valueOf(code), name);
            return this;
        }

        public Builder setLocalizedName(EnumMap<LanguageCode, String> languageCodeStringMap) {
            this.localizedName = languageCodeStringMap;
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
            Language doc = new Language();
            setDefaultFields(doc);
            doc.setCode(code);
            doc.setOn(isOn);
            doc.setPosition(position);
            doc.setLocalizedName(localizedName);
            return doc;
        }



    }

}
