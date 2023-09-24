package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Locality extends SimpleReferenceEntity {

    private List<Street> streets;

    private List<CityDistrict> cityDistricts;

    private LocalityType type;

    private Region region;


    private District district;

    private boolean isDistrictCenter;


}
