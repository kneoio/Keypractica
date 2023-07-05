package com.semantyca.officeframe.dto;

import com.semantyca.model.Language;
import com.semantyca.projects.model.constants.ProjectStatusType;

public record OrganizationDTO(String name, ProjectStatusType status, Language primaryLang, String coder, int position) {

}
