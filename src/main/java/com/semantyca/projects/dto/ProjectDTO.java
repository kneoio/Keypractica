package com.semantyca.projects.dto;

import com.semantyca.model.Language;
import com.semantyca.projects.model.constants.ProjectStatusType;

public record ProjectDTO(String name, ProjectStatusType status, Language primaryLang, int position) {
}
