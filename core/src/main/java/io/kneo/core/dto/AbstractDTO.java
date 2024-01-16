package io.kneo.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
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
    @JsonView(Views.DetailView.class)
    protected String author;
    @JsonView(Views.DetailView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    protected ZonedDateTime regDate;
    @JsonView(Views.DetailView.class)
    protected String lastModifier;
    @JsonView(Views.DetailView.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    protected ZonedDateTime lastModifiedDate;

}
