package com.semantyca.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Street extends SimpleReferenceEntity {

    private Locality locality;

    private CityDistrict cityDistrict;

    private int streetId;

    private Set<String> altName = new HashSet<>();


}
