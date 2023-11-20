package io.kneo.projects.dto.actions;

import io.kneo.core.dto.actions.ActionBar;
import io.kneo.core.dto.actions.ActionsFactory;


public class ProjectActionsFactory {

    public static ActionBar getViewActions() {
        return ActionsFactory.getDefault();
    }

}
