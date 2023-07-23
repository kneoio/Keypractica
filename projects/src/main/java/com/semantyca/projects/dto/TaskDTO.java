package com.semantyca.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.dto.AbstractDTO;
import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.projects.model.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class TaskDTO extends AbstractDTO {
    String body;
    String assignee;
    Map<LanguageCode, String> taskType;
    ProjectDTO project;
    Task parent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm") ZonedDateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm") ZonedDateTime targetDate;
    int status;
    int priority;
    List<RLSDTO> rls;

}
