package com.semantyca.officeframe.dto;


import com.semantyca.core.model.Language;

public record OrganizationDTO(String name, Language primaryLang, String coder, int position) {

}
