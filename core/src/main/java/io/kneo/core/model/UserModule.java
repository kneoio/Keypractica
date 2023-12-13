package io.kneo.core.model;

import io.kneo.core.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumMap;

@Setter
@Getter
@NoArgsConstructor
public class UserModule extends SimpleReferenceEntity {
    private EnumMap<LanguageCode, String> localizedDescription = new EnumMap<>(LanguageCode.class);
    private boolean invisible;
    private boolean isPublic;
    private String theme;
    private int position;

}
