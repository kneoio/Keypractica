package io.kneo.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.model.SimpleReferenceEntity;
import io.kneo.officeframe.model.cnst.LocalityCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class LocalityType extends SimpleReferenceEntity {

    private LocalityCode code = LocalityCode.UNKNOWN;


}
