package com.semantyca.core.dto.document;

import com.semantyca.core.dto.AbstractDTO;
import com.semantyca.core.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class LanguageDTO extends AbstractDTO{
    LanguageCode code;
    Map<LanguageCode, String> localizedNames;
}
