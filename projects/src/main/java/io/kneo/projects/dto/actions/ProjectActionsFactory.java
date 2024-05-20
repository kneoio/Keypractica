package io.kneo.projects.dto.actions;

import io.kneo.core.dto.actions.Action;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.actions.ContextAction;
import io.kneo.core.dto.cnst.RunMode;
import io.kneo.core.model.user.IRole;

import java.util.List;

public class ProjectActionsFactory {

    public static ContextAction getViewActions(List<IRole> activatedRoles) {
        ContextAction actions = ActionsFactory.getDefault();
        Action action = new Action();
        action.setIsOn(RunMode.ON);
        action.setCaption("new_project");
        actions.addAction(action);
        return actions;
    }

}
