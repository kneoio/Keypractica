package io.kneo.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Street extends SimpleReferenceEntity {
    private Locality locality;
    private CityDistrict cityDistrict;
    private int streetId;
    private Set<String> altName = new HashSet<>();


}
