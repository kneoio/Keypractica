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
public class Locality extends SimpleReferenceEntity {
    private List<Street> streets;
    private List<CityDistrict> cityDistricts;
    private LocalityType type;
    private Region region;
    private District district;
    private boolean isDistrictCenter;
}
