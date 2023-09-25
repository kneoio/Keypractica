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
public class ModuleDTO extends AbstractDTO {
    String identifier;
    boolean isOn;
    Map<LanguageCode, String> localizedName;
    Map<LanguageCode, String> localizedDescription;
}
