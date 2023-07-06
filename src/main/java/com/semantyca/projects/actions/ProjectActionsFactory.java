package com.semantyca.projects.actions;

import com.semantyca.dto.actions.ActionBar;
import com.semantyca.dto.actions.DefaultActionsFactory;

public class ProjectActionsFactory {

    public static ActionBar getViewActions() {
        return DefaultActionsFactory.get();
    }

}
