package io.kneo.core.dto.cnst;

public enum AccessLevel {
    NO_ACCESS(0,"restricted"), READ_ONLY(1,"read"), EDIT_IS_ALLOWED(2, "edit"), EDIT_AND_DELETE_ARE_ALLOWED(3,"full");
    private int code;
    private String alias;

    AccessLevel(int code, String alias) {
        this.code = code;
        this.alias = alias;
    }

    public static AccessLevel getType(int code) {
        for (AccessLevel type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return NO_ACCESS;
    }
    AccessLevel(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
