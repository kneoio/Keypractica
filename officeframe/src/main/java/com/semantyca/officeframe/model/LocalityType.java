package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.officeframe.model.cnst.LocalityCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class LocalityType extends SimpleReferenceEntity {

    private LocalityCode code = LocalityCode.UNKNOWN;


}
