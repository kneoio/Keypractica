package io.kneo.core.dto.actions;

import io.kneo.core.dto.actions.cnst.ActionType;
import io.kneo.core.localization.LanguageCode;

public class ActionsFactory {

    public static ActionBox getDefaultViewActions(LanguageCode lang) {
        ActionBox bar = new ActionBox();
        bar.setCaption("Available actions");
        bar.setHint("The actions available actions based on your credentials");
        bar.addAction(new Action(ActionType.CREATE.getAlias()));
        bar.addAction(new Action(ActionType.DELETE.getAlias()));
        return bar;
    }

    public static ActionBox getDefaultFormActions(LanguageCode lang) {
        ActionBox bar = new ActionBox();
        bar.setCaption("Available actions");
        bar.setHint("The actions available actions based on your credentials");
        bar.addAction(new Action(ActionType.SAVE.getAlias()));
        bar.addAction(new Action(ActionType.CLOSE.getAlias()));
        return bar;
    }
}
