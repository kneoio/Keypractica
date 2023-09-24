package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.officeframe.model.cnst.LocalityCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalityType extends SimpleReferenceEntity {

    private LocalityCode code = LocalityCode.UNKNOWN;


}
