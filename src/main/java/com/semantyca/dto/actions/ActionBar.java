package com.semantyca.dto.actions;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@NoArgsConstructor
public class ActionBar {
    public String caption;
    public String hint;
    private ArrayList<Action> actions = new ArrayList<>();

    public ActionBar addAction(Action action) {
        actions.add(action);
        return this;
    }
}
