package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.officeframe.model.cnst.RegionCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionType extends SimpleReferenceEntity {

    private RegionCode code = RegionCode.UNKNOWN;

    public RegionCode getCode() {
        return code;
    }

    public void setCode(RegionCode code) {
        this.code = code;
    }


}
