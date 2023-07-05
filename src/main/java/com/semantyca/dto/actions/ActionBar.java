package com.semantyca.dto.actions;


import java.util.ArrayList;
import java.util.List;

public class ActionBar {
    public String caption;
    public String hint;
    private ArrayList<Action> actions = new ArrayList<>();

    public ActionBar addAction(Action action) {
        actions.add(action);
        return this;
    }

    public ActionBar addAction(List<Action> actions) {
        for (Action action : actions) {
            addAction(action);
        }
        return this;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }
}
