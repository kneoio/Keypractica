package com.semantyca.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DepartmentDTO(String name) {

}
