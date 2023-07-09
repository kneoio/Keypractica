package com.semantyca.core.model;

import com.semantyca.core.localization.LanguageCode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleReferenceEntity extends DataEntity<UUID> {

    protected String identifier;

    private Map<LanguageCode, String> locName = new HashMap<LanguageCode, String>();

    public String getIdentifier() {
        return identifier;
    }

    public Map<LanguageCode, String> getLocName() {
        return locName;
    }

    public void setLocName(Map<LanguageCode, String> locName) {
        this.locName = locName;
    }

    public String getLocName(LanguageCode lang) {
        try {
            String val = locName.get(lang);
            if (val != null && (!val.isEmpty())) {
                return val;
            } else {
                return identifier;
            }
        } catch (Exception e) {
            return identifier;
        }
    }

    public void setLocName(String val, LanguageCode languageCode) {
        this.locName.put(languageCode, val);
    }

}
