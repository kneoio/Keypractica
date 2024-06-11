package io.kneo.core.dto.actions.cnst;

import lombok.Getter;

@Getter
public enum ActionType {
    CLOSE("close"), CREATE("create"), ARCHIVE("archive"), DELETE("delete"), SAVE("save");

    private final String alias;

    ActionType(String alias) {
        this.alias = alias;
    }

}
