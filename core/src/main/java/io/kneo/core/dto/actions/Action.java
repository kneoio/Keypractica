package io.kneo.core.dto.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.kneo.core.dto.cnst.RunMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class Action {
    @JsonIgnore
    private RunMode isOn = RunMode.ON;
    private String caption;

    public Action(String alias) {
        this.caption = alias;
    }
}
