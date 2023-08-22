package com.semantyca.core.model.cnst;

public enum RoleType {
    UNKNOWN(0, "unknown", "???"),
    USER(101, "user", "user_role"),
    SUPERVISOR(102,"supervisor" ,"supervisor_role"),
    CUSTOM(200,"custom" ,"custom_role");

    private final int code;
    private final String name;
    private final String shortName;

    RoleType(int code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public static RoleType getType(int code) {
        for (RoleType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getShortName() {
        return shortName;
    }

}
