package io.kneo.core.dto.document;

import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.localization.LanguageCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO extends AbstractDTO {
    LanguageCode code;
    Map<LanguageCode, String> localizedNames;
}
