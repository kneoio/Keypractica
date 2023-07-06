package com.semantyca.projects.dto;

import com.semantyca.projects.model.constants.ProjectStatusType;

import java.time.LocalDate;
import java.util.UUID;

public record ProjectDTO(UUID id, String name, ProjectStatusType status, LocalDate finishDate, String manager) {

}
