package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class OrganizationDTO extends AbstractReferenceDTO {
    private OrgCategoryDTO orgCategory;
    private boolean isPrimary;
    private String bizID;
    private int rank;
    EnumMap<LanguageCode, String> localizedName = new EnumMap<>(LanguageCode.class);
}
