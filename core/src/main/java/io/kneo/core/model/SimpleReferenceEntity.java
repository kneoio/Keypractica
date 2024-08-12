package io.kneo.core.model;

import io.kneo.core.localization.LanguageCode;
import io.kneo.core.server.Environment;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.UUID;

@Setter
public class SimpleReferenceEntity extends DataEntity<UUID> {
    @Getter
    protected String identifier;
    protected EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);

    public EnumMap<LanguageCode, String> getLocalizedName() {
        for (LanguageCode code : Environment.AVAILABLE_LANGUAGES) {
            if (!localizedName.containsKey(code)) {
                localizedName.put(code, "");
            }
        }

        return localizedName;
    }

    public String getLocalizedName(LanguageCode lang) {
        try {
            String val = localizedName.get(lang);
            if (val != null && !val.isEmpty()) {
                return val;
            } else {
                return identifier;
            }
        } catch (Exception e) {
            return identifier;
        }
    }
}
