package com.semantyca.dto.cnst;

public enum PayloadType {
    ACTIONS("actions"), EXCEPTION("exception"), TEXT("text");

    private String alias;

    PayloadType(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
