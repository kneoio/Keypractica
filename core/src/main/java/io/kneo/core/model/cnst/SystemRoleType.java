package io.kneo.core.model.cnst;

import lombok.Getter;

@Getter
public enum SystemRoleType {
    UNKNOWN(0, "unknown", "???"),
    OBSERVER(101, "observer", "obsv"),
    SUPERVISOR(102,"supervisor" ,"super");

    private final int code;
    private final String name;
    private final String shortName;

    SystemRoleType(int code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public static SystemRoleType getType(int code) {
        for (SystemRoleType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
