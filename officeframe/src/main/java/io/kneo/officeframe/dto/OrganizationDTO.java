package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrganizationDTO(String name, String bizID) {

}
