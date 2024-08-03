package io.kneo.officeframe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.AbstractReferenceDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class EmployeeDTO extends AbstractReferenceDTO {
    @Positive(message = "{employee.userId.invalid}")
    long userId;
    LocalDate birthDate;
    @NotNull(message = "{employee.org.invalid}")
    OrganizationDTO org;
    @NotNull(message = "{employee.dep.invalid}")
    DepartmentDTO dep;
    @NotNull(message = "{employee.position.invalid}")
    PositionDTO position;
    @Positive(message = "{employee.rank.invalid}")
    int rank = 999;
    String phone;

}
