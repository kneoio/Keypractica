package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeDTO(UUID id, String name) {

}
