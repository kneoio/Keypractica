package com.semantyca.core.model.user;


import com.semantyca.core.localization.LanguageCode;

import java.util.Map;

public interface IRole {

    String getIdentifier();

    void setIdentifier(String name);

    Map<LanguageCode, String> getLocName();

    void setLocName(Map<LanguageCode, String> locName);

    Map<LanguageCode, String> getLocalizedDescr();


}
