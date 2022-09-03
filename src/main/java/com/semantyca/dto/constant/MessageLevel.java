package com.semantyca.dto.constant;

public enum MessageLevel {
    SUCCESS("success"), MISFORTUNE("misfortune"), FAILURE("failure");

    private String alias;

    MessageLevel(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }
}
