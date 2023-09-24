package com.semantyca.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Label extends SimpleReferenceEntity {

    private String color;

    private Label parent;

    private boolean hidden;

    private String category;


}
