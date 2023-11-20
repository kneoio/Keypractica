package io.kneo.core.dto.actions;

import io.kneo.core.dto.actions.cnst.ActionType;
import io.kneo.core.dto.cnst.RunMode;

public class ActionsFactory {

    public static ActionBar getDefault() {
        ActionBar bar = new ActionBar();
        Action action = new Action();
        action.setIsOn(RunMode.ON);
        action.setCaption(ActionType.CLOSE.getAlias());
        bar.addAction(action);
        return bar;
    }

}
