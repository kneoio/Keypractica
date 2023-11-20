package io.kneo.core.dto.actions.cnst;

public enum ActionType {
    CLOSE("close");

    private String alias;

    ActionType(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
