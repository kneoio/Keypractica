package io.kneo.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Region extends SimpleReferenceEntity {
    private List<District> districts;
    private List<Locality> localities;
    private Country country;
    private RegionType type;
    private boolean isPrimary;
    private String latLng;


}
