
package io.kneo.projects.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.officeframe.dto.EmployeeDTO;
import io.kneo.officeframe.dto.LabelDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@Deprecated
public class TaskTemplateDTO {
    List<EmployeeDTO> availableAssignee;
    List<TaskTypeDTO> availableTaskType;
    List<ProjectDTO> availableProject;
    LocalDate targetDate;
    int priority;
    List<LabelDTO> availableLabels;
}
