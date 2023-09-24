package com.semantyca.projects.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.dto.AbstractDTO;
import com.semantyca.core.dto.rls.RLSDTO;
import com.semantyca.core.model.constants.ProjectStatusType;
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
public class ProjectDTO extends AbstractDTO {
        private String name;
        private ProjectStatusType status;
        private LocalDate finishDate;
        private String manager;
        private String coder;
        private String tester;
        private List<RLSDTO> rls;

}
