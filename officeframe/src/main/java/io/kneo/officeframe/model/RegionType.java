package io.kneo.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.SimpleReferenceEntity;
import io.kneo.officeframe.model.cnst.RegionCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class RegionType extends SimpleReferenceEntity {
    private RegionCode code = RegionCode.UNKNOWN;

}
