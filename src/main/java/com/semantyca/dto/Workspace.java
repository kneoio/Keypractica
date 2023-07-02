package com.semantyca.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semantyca.dto.view.ViewPage;
import com.semantyca.service.LanguageService;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Workspace extends Page {

    public Workspace(LanguageService service) throws JsonProcessingException {
        addPayload("appname", "Jirascope");
        addPayload(new ViewPage(service.getAll()));
        System.out.println(new ObjectMapper().writeValueAsString(payload));
    }

}
