package io.kneo.projects.dto.actions;

import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.model.user.IRole;

import java.util.List;

public class TaskActionsFactory {
    public static ContextAction getViewActions(List<IRole> roles) {
        return ActionsFactory.getDefault();
    }

}
