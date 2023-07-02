package com.semantyca.dto.document;

import com.semantyca.localization.LanguageCode;

import java.util.Map;

public record LanguageDTO(String code, Map<LanguageCode, String> localizedNames) {
}
