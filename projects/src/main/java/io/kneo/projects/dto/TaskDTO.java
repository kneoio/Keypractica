
package io.kneo.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.projects.model.Task;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    String regNumber;
    private String title;
    @NotNull(message = "Body must not be null")
    @NotEmpty(message = "Body must not be empty")
    String body;
    AssigneeDTO assignee;
    Map<LanguageCode, String> taskType;
    ProjectDTO project;
    Task parent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    ZonedDateTime startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    ZonedDateTime targetDate;
    int status;
    int priority;
    private String cancellationComment;
    List<LabelDTO> labels;
    List<RLSDTO> rls;

}
