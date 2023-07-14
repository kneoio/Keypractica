package com.semantyca.core.model.cnst;

public enum ModuleType {
    UNKNOWN(0, "unknown", "???"),
    DICTIONARY(100, "dictionary", "dict"),
    OFFICEFRAME(101, "officeframe", "dict"),
    BIZ(102, "business", "dict");

    private int code;
    private String name;
    private String shortName;

    ModuleType(int code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public static ModuleType getType(int code) {
        for (ModuleType type : values()) {
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
