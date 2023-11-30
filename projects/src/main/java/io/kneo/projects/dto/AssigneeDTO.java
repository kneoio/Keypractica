
package io.kneo.projects.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class AssigneeDTO {
    long id;
    String fullName;
    boolean isAvailable;
}
