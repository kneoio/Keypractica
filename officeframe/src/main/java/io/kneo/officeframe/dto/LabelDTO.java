package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.AbstractReferenceDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class LabelDTO extends AbstractReferenceDTO {
    private String color;
    private LabelDTO parent;
    private boolean hidden;
    private String identifier;
    private String category;

    public LabelDTO(String identifier) {
        this.identifier = identifier;
    }
}
