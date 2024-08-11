package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.AbstractReferenceDTO;
import io.kneo.core.dto.Views;
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
    @JsonView(Views.DetailView.class)
    private String color;
    @JsonView(Views.DetailView.class)
    private UUID parent;
    @JsonView(Views.DetailView.class)
    private boolean hidden;
    private String identifier;
    @JsonView(Views.DetailView.class)
    private String category;

    public LabelDTO(String id) {
        this.id = UUID.fromString(id);
    }
}
