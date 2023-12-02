package io.kneo.core.model;

import io.kneo.core.localization.LanguageCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//@Indexed
@Getter
@Setter
public class SimpleReferenceEntity extends DataEntity<UUID> {
    protected String identifier;
    protected Map<LanguageCode, String> localizedName = new HashMap<>();

    public String getLocName(LanguageCode lang) {
        try {
            String val = localizedName.get(lang);
            if (val != null && (!val.isEmpty())) {
                return val;
            } else {
                return identifier;
            }
        } catch (Exception e) {
            return identifier;
        }
    }

}