package com.semantyca.officeframe.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.SimpleReferenceEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Label extends SimpleReferenceEntity {

    private String color;

    private Label parent;

    private boolean hidden;

    private String category;


}
