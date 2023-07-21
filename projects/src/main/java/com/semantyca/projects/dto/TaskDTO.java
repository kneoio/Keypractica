package com.semantyca.projects.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.core.dto.IDTO;
import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.model.embedded.RLS;
import com.semantyca.projects.model.Project;
import com.semantyca.projects.model.Task;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
//public record TaskDTO(UUID id, String regNumber, String body, ZonedDateTime startDate, ZonedDateTime targetDate) {
public record TaskDTO(UUID id, String regNumber, String body, String assignee, java.util.Map<LanguageCode, String> taskType, Project project, Task parent, ZonedDateTime startDate, ZonedDateTime targetDate, int status , int priority, List<RLS> rls) implements IDTO {

}
