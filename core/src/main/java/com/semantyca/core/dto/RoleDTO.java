package com.semantyca.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.semantyca.core.localization.LanguageCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({"identifier", "author", "regDate", "lastModifier", "lastModifiedDate"})
public class RoleDTO extends AbstractReferenceDTO {
    Map<LanguageCode, String> localizedName;
    Map<LanguageCode, String> localizedDescription;

}
