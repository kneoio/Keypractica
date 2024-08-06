
package io.kneo.projects.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.LabelDTO;
import io.kneo.officeframe.dto.TaskTypeDTO;
import io.kneo.projects.model.Task;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class TaskDTO extends AbstractDTO {
    @JsonView(Views.DetailView.class)
    String regNumber;
    private String title;
    @NotNull(message = "Body must not be null")
    @NotEmpty(message = "Body must not be empty")
    @JsonView(Views.DetailView.class)
    String body;
    @NotNull(message = "Assignee must not be null")
    @JsonView(Views.DetailView.class)
    EmployeeDTO assignee;
    @NotNull(message = "Task type must not be null")
    @JsonView(Views.DetailView.class)
    TaskTypeDTO taskType;
    @NotNull(message = "Project must not be null")
    @JsonView(Views.DetailView.class)
    ProjectDTO project;
    @JsonView(Views.DetailView.class)
    Task parent;
    @NotNull(message = "Start date must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @JsonView(Views.DetailView.class)
    LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @JsonView(Views.DetailView.class)
    ZonedDateTime targetDate;
    @JsonView(Views.DetailView.class)
    int status;
    @JsonView(Views.DetailView.class)
    int priority;
    @JsonView(Views.DetailView.class)
    private String cancellationComment;
    @JsonView(Views.DetailView.class)
    List<LabelDTO> labels = new ArrayList<>();
    @JsonView(Views.DetailView.class)
    List<RLSDTO> rls = new ArrayList<>();

}
