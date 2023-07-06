package com.semantyca.dto.actions;

import com.semantyca.dto.cnst.RunMode;

public class DefaultActionsFactory {

    public static ActionBar get() {
        ActionBar bar = new ActionBar();
        Action action = new Action();
        action.setIsOn(RunMode.ON);
        action.setCaption("close");
        bar.addAction(action);
        return bar;
    }

}
