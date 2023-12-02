
package io.kneo.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.projects.model.Task;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @NotNull(message = "Assignee must not be null")
    AssigneeDTO assignee;
    @NotNull(message = "Task type must not be null")
    TaskTypeDTO taskType;
    @NotNull(message = "Project must not be null")
    ProjectDTO project;
    Task parent;
    @NotNull(message = "Start date must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    LocalDate targetDate;
    int status;
    int priority;
    private String cancellationComment;
    List<LabelDTO> labels = new ArrayList<>();
    List<RLSDTO> rls = new ArrayList<>();

}
