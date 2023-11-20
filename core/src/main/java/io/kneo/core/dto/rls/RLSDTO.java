package io.kneo.core.dto.rls;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RLSDTO(String reader, String accessLevel, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm") ZonedDateTime readingTime) {
}
