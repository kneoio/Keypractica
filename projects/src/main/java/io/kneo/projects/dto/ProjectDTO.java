package io.kneo.projects.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.kneo.core.dto.AbstractDTO;
import io.kneo.core.dto.Views;
import io.kneo.core.dto.document.UserDTO;
import io.kneo.core.dto.rls.RLSDTO;
import io.kneo.core.localization.LanguageCode;
import io.kneo.projects.model.cnst.ProjectStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
public class ProjectDTO extends AbstractDTO {
    private String name;
    private String description;
    @JsonView(Views.DetailView.class)
    @NotNull
    private ProjectStatusType status;
    @JsonView(Views.DetailView.class)
    private LocalDate startDate;
    private LocalDate finishDate;

    @JsonView(Views.DetailView.class)
    private UserDTO manager;
    @JsonView(Views.DetailView.class)
    private UserDTO coder;
    @JsonView(Views.DetailView.class)
    private UserDTO tester;

    @JsonView(Views.DetailView.class)
    private List<RLSDTO> rls = new ArrayList<>();
    private LanguageCode primaryLang;

    public ProjectDTO(String id) {
        this.id = UUID.fromString(id);
    }

}
