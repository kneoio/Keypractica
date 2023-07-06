package com.semantyca.dto.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.semantyca.dto.cnst.RunMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Action {
    @JsonIgnore
    private RunMode isOn = RunMode.HIDE;
    private String caption;
}
