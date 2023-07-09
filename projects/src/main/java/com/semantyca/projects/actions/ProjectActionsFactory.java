package com.semantyca.projects.actions;

import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.actions.ActionsFactory;


public class ProjectActionsFactory {

    public static ActionBar getViewActions() {
        return ActionsFactory.getDefault();
    }

}
