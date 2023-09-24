package com.semantyca.officeframe.model;


import com.semantyca.core.model.SimpleReferenceEntity;

import java.util.List;

public class Region extends SimpleReferenceEntity {

    private List<District> districts;


    private List<Locality> localities;

    private Country country;

    private RegionType type;

    private boolean isPrimary;

    private String latLng;


}
