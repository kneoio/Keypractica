package io.kneo.core.dto.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.kneo.core.dto.AbstractReferenceDTO;
import io.kneo.core.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.EnumMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({"identifier", "author", "regDate", "lastModifier", "lastModifiedDate"})
public class RoleDTO extends AbstractReferenceDTO {
    EnumMap<LanguageCode, String> localizedName;
    EnumMap<LanguageCode, String> localizedDescription;
}
