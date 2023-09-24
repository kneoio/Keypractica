package com.semantyca.core.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractReferenceDTO extends AbstractDTO {
    String identifier;
}
