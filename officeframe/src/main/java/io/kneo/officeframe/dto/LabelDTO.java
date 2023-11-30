package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.AbstractReferenceDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class LabelDTO extends AbstractReferenceDTO {
    private UUID id;
    private String color;
    private LabelDTO parent;
    private boolean hidden;
    private String identifier;
    private String category;
}
