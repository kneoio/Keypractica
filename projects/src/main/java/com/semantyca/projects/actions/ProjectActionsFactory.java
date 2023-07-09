package com.semantyca.projects.actions;

import com.semantyca.core.dto.actions.ActionBar;
import com.semantyca.core.dto.actions.DefaultActionsFactory;

public class ProjectActionsFactory {

    public static ActionBar getViewActions() {
        return DefaultActionsFactory.get();
    }

}
