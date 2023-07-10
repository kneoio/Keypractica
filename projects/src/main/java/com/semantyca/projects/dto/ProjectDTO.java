package com.semantyca.projects.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.model.constants.ProjectStatusType;
import com.semantyca.core.model.embedded.RLS;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProjectDTO(UUID id, String name, ProjectStatusType status, LocalDate finishDate, String manager, String coder, String tester, List<RLS> rls) {

}
