package io.kneo.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractDTO implements IDTO {
    protected UUID id;
    protected String author;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    protected ZonedDateTime regDate;
    protected String lastModifier;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    protected ZonedDateTime lastModifiedDate;

}
