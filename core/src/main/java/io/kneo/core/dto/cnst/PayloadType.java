package io.kneo.core.dto.cnst;

public enum PayloadType {
    ACTIONS("actions"), EXCEPTION("exception"), TEXT("text"), VIEW_OPTIONS("view_options"), VIEW_DATA("view_data"), FORM_DATA("form_data");

    private String alias;

    PayloadType(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
