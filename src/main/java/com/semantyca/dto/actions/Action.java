package com.semantyca.dto.actions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.dto.cnst.RunMode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Action {
    private RunMode isOn;
    private String caption;


}
