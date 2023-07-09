package com.semantyca.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import com.semantyca.officeframe.cnst.CountryCode;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Country extends SimpleReferenceEntity {

    private CountryCode code = CountryCode.UNKNOWN;

    public CountryCode getCode() {
        return code;
    }

    public void setCode(CountryCode code) {
        this.code = code;
    }





}
