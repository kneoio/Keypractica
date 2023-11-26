package io.kneo.projects.dto.actions;

import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.actions.ContextAction;

public class ProjectActionsFactory {

    public static ContextAction getViewActions() {
        return ActionsFactory.getDefault();
    }

}
