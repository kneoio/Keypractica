package io.kneo.projects.dto.actions;

import io.kneo.core.dto.actions.Action;
import io.kneo.core.dto.actions.ActionBox;
import io.kneo.core.dto.actions.ActionsFactory;
import io.kneo.core.dto.cnst.RunMode;
import io.kneo.core.localization.LanguageCode;

public class TaskActionsFactory {
    public static ActionBox getViewActions(LanguageCode lang) {
        ActionBox actions = ActionsFactory.getDefaultViewActions(lang);
        Action action = new Action();
        action.setIsOn(RunMode.ON);
        action.setCaption("new_task");
        actions.addAction(action);
        return actions;
    }

}
