package com.semantyca.officeframe.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class District extends SimpleReferenceEntity {
    private List<Locality> localities;
    private Region region;
    private String latLng;



}
