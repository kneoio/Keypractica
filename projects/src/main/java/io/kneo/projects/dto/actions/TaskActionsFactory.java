package io.kneo.projects.dto.actions;

import io.kneo.core.dto.actions.Action;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.cnst.RunMode;
import io.kneo.core.localization.LanguageCode;
import io.kneo.core.model.user.IRole;

import java.util.List;

public class TaskActionsFactory {
    public static ActionBox getViewActions(List<IRole> roles) {
        ActionBox actions = ActionsFactory.getDefaultViewActions(LanguageCode.ENG);
        Action action = new Action();
        action.setIsOn(RunMode.ON);
        action.setCaption("new_task");
        actions.addAction(action);
        return actions;
    }

}
