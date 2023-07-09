package com.semantyca.core.dto.document;

import com.semantyca.core.dto.IDTO;
import com.semantyca.core.localization.LanguageCode;

import java.util.Map;

public record LanguageDTO(String code, Map<LanguageCode, String> localizedNames) implements IDTO {


}
