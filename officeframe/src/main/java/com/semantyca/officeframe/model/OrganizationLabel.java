package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class OrganizationLabel extends SimpleReferenceEntity {
    private List<Organization> labels;
    private Map<LanguageCode, String> localizedDescr;

}
