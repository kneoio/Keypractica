package io.kneo.core.dto.document;

import io.kneo.core.dto.AbstractReferenceDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.server.Environment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class ModuleDTO extends AbstractReferenceDTO {
    String company = Environment.companyShortName;
    boolean isOn;
    Map<LanguageCode, String> localizedName;
    Map<LanguageCode, String> localizedDescription;
}
