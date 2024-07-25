package io.kneo.core.dto;

import io.kneo.core.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.EnumMap;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractReferenceDTO extends AbstractDTO {
    protected String identifier;
    EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
}
