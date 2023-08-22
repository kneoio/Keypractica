package com.semantyca.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({"identifier", "author", "regDate", "lastModifier", "lastModifiedDate"})
public class RoleDTO extends AbstractReferenceDTO {

}
